package com.wait.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wait.server.dto.MatchCandidateVO;
import com.wait.server.entity.UserEntity;
import com.wait.server.entity.UserProfileEntity;
import com.wait.server.exception.BusinessException;
import com.wait.server.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final UserService userService;
    private final UserProfileService userProfileService;
    private final ConversationService conversationService;

    public List<MatchCandidateVO> candidates(Long meId, BigDecimal lng, BigDecimal lat, Double radiusKm) {
        UserEntity me = userService.getById(meId);
        if (me == null) throw new BusinessException(404, "user not found");
        if (!"FREE".equals(me.getEngagementState())) {
            throw new BusinessException(409, "you are currently ENGAGED, cannot start new greeting");
        }
        // 1. 取所有 FREE、可见、非自己的用户
        List<UserEntity> pool = userService.list(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getStatus, 1)
                .eq(UserEntity::getMatchVisibility, 1)
                .eq(UserEntity::getEngagementState, "FREE")
                .ne(UserEntity::getId, meId));

        if (pool.isEmpty()) return List.of();

        // 2. 排除正在进行中的对话双方（PENDING 中的目标也算占用对方）
        Set<Long> excludeIds = conversationService.engagedPartnerIds(meId);
        pool = pool.stream().filter(u -> !excludeIds.contains(u.getId())).collect(Collectors.toList());

        // 3. 计算距离（按需）+ 标签匹配度，组装 VO
        UserProfileEntity myProfile = userProfileService.findOrCreateByUserId(meId);
        Map<String, Object> myPresetReq = JsonUtil.toMap(myProfile.getRequirementPreset());
        List<Map<String, String>> myCustomReq = JsonUtil.toMapList(myProfile.getRequirementCustom());

        List<MatchCandidateVO> result = new ArrayList<>();
        for (UserEntity u : pool) {
            UserProfileEntity p = userProfileService.findOrCreateByUserId(u.getId());
            MatchCandidateVO vo = toVO(u, p);

            // 距离
            Double dist = distanceKm(lng, lat, u.getLastLng(), u.getLastLat());
            vo.setDistanceKm(dist);

            // 标签匹配度
            int score = computeMatchScore(myPresetReq, myCustomReq, p, myProfile);
            vo.setMatchScore(score);

            // 对方对我的要求
            Map<String, Object> otherPreset = JsonUtil.toMap(p.getRequirementPreset());
            List<Map<String, String>> otherCustom = JsonUtil.toMapList(p.getRequirementCustom());
            vo.setRequirementPreset(otherPreset);
            vo.setRequirementCustom(otherCustom);
            vo.setRequirementMatched(checkRequirementSatisfied(otherPreset, otherCustom, myProfile));

            result.add(vo);
        }

        // 4. 过滤：当请求了位置半径，且候选无定位时不剔除（仅排后）
        // 5. 排序：匹配度降序 → 活跃度降序
        result.sort(Comparator
                .comparing(MatchCandidateVO::getMatchScore, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(MatchCandidateVO::getDistanceKm, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Comparator.comparing((MatchCandidateVO v) -> lastActiveAtOf(v.getUserId()))
                        .reversed()));

        if (radiusKm != null) {
            result = result.stream()
                    .filter(v -> v.getDistanceKm() == null || v.getDistanceKm() <= radiusKm)
                    .collect(Collectors.toList());
        }

        return result;
    }

    private LocalDateTime lastActiveAtOf(Long userId) {
        UserEntity u = userService.getById(userId);
        return u == null ? LocalDateTime.MIN : (u.getLastActiveAt() == null ? LocalDateTime.MIN : u.getLastActiveAt());
    }

    private Double distanceKm(BigDecimal lng1, BigDecimal lat1, BigDecimal lng2, BigDecimal lat2) {
        if (lng1 == null || lat1 == null || lng2 == null || lat2 == null) return null;
        double R = 6371.0;
        double dLat = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double dLng = Math.toRadians(lng2.doubleValue() - lng1.doubleValue());
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1.doubleValue()))
                * Math.cos(Math.toRadians(lat2.doubleValue()))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    /** 我对对方的要求 是否满足 → 不满足直接 0 分 */
    private int computeMatchScore(Map<String, Object> myReqPreset,
                                  List<Map<String, String>> myReqCustom,
                                  UserProfileEntity other,
                                  UserProfileEntity myProfile) {
        // preset: 硬匹配
        if (myReqPreset != null) {
            for (Map.Entry<String, Object> e : myReqPreset.entrySet()) {
                String k = e.getKey();
                Object v = e.getValue();
                if (!isPresetSatisfied(k, v, other)) return 0;
            }
        }
        // custom: 硬匹配
        if (myReqCustom != null) {
            for (Map<String, String> c : myReqCustom) {
                String k = c.get("key");
                String v = c.get("value");
                if (!isCustomSatisfied(k, v, other)) return 0;
            }
        }

        // 标签重合度评分（hobbies + tags）
        List<String> myTags = JsonUtil.toStringList(myProfile.getTags());
        List<String> myHobbies = JsonUtil.toStringList(myProfile.getHobbies());
        List<String> otherTags = JsonUtil.toStringList(other.getTags());
        List<String> otherHobbies = JsonUtil.toStringList(other.getHobbies());

        Set<String> mySet = new java.util.HashSet<>();
        mySet.addAll(myTags);
        mySet.addAll(myHobbies);
        Set<String> otherSet = new java.util.HashSet<>();
        otherSet.addAll(otherTags);
        otherSet.addAll(otherHobbies);

        if (mySet.isEmpty() && otherSet.isEmpty()) return 50;
        long inter = mySet.stream().filter(otherSet::contains).count();
        long union = mySet.size() + otherSet.size() - inter;
        if (union == 0) return 50;
        return (int) (inter * 100.0 / union);
    }

    private boolean isPresetSatisfied(String key, Object value, UserProfileEntity other) {
        if (value == null) return true;
        Map<String, Object> otherMap = toFlatMap(other);
        Object otherVal = otherMap.get(key);
        if (otherVal == null) return false;
        // 范围型字段
        if (key.endsWith("Min")) {
            String base = key.substring(0, key.length() - 3);
            Integer o = (Integer) otherMap.get(base);
            Integer v = toInt(value);
            return o != null && v != null && o >= v;
        }
        if (key.endsWith("Max")) {
            String base = key.substring(0, key.length() - 3);
            Integer o = (Integer) otherMap.get(base);
            Integer v = toInt(value);
            return o != null && v != null && o <= v;
        }
        return String.valueOf(value).equalsIgnoreCase(String.valueOf(otherVal));
    }

    private boolean isCustomSatisfied(String key, String value, UserProfileEntity other) {
        Map<String, Object> otherMap = toFlatMap(other);
        Object o = otherMap.get(key);
        if (o == null) return false;
        return String.valueOf(value).equalsIgnoreCase(String.valueOf(o));
    }

    private boolean checkRequirementSatisfied(Map<String, Object> reqPreset,
                                              List<Map<String, String>> reqCustom,
                                              UserProfileEntity me) {
        if (reqPreset != null) {
            for (Map.Entry<String, Object> e : reqPreset.entrySet()) {
                if (!isPresetSatisfied(e.getKey(), e.getValue(), me)) return false;
            }
        }
        if (reqCustom != null) {
            for (Map<String, String> c : reqCustom) {
                if (!isCustomSatisfied(c.get("key"), c.get("value"), me)) return false;
            }
        }
        return true;
    }

    private Map<String, Object> toFlatMap(UserProfileEntity p) {
        Map<String, Object> m = new HashMap<>();
        m.put("age", p.getAge());
        m.put("height", p.getHeight());
        m.put("education", p.getEducation());
        m.put("city", p.getCity());
        m.put("district", p.getDistrict());
        m.put("profession", p.getProfession());
        return m;
    }

    private Integer toInt(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.intValue();
        try { return Integer.parseInt(String.valueOf(o)); } catch (Exception e) { return null; }
    }

    private MatchCandidateVO toVO(UserEntity u, UserProfileEntity p) {
        MatchCandidateVO vo = new MatchCandidateVO();
        vo.setUserId(u.getId());
        vo.setNickname(u.getNickname());
        vo.setAvatar(u.getAvatar());
        vo.setGender(u.getGender());
        vo.setAge(p.getAge());
        vo.setCity(p.getCity());
        vo.setDistrict(p.getDistrict());
        vo.setProfession(p.getProfession());
        vo.setHeight(p.getHeight());
        vo.setEducation(p.getEducation());
        vo.setPersonality(JsonUtil.toStringList(p.getPersonality()));
        vo.setAppearances(JsonUtil.toStringList(p.getAppearances()));
        vo.setHobbies(JsonUtil.toStringList(p.getHobbies()));
        vo.setTags(JsonUtil.toStringList(p.getTags()));
        return vo;
    }
}
