package com.wait.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wait.server.dto.ProfileVO;
import com.wait.server.dto.UpdateProfileDTO;
import com.wait.server.entity.RegionEntity;
import com.wait.server.entity.UserEntity;
import com.wait.server.entity.UserProfileEntity;
import com.wait.server.mapper.RegionMapper;
import com.wait.server.mapper.UserProfileMapper;
import com.wait.server.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserProfileService extends ServiceImpl<UserProfileMapper, UserProfileEntity> {

    private final UserService userService;
    private final RegionMapper regionMapper;

    public UserProfileEntity findOrCreateByUserId(Long userId) {
        UserProfileEntity p = getOne(new LambdaQueryWrapper<UserProfileEntity>()
                .eq(UserProfileEntity::getUserId, userId)
                .last("LIMIT 1"));
        if (p == null) {
            p = new UserProfileEntity();
            p.setUserId(userId);
            save(p);
        }
        return p;
    }

    public ProfileVO toVO(UserEntity user, UserProfileEntity p) {
        ProfileVO vo = new ProfileVO();
        vo.setUserId(user.getId());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setGender(user.getGender());
        vo.setRealName(p.getRealName());
        vo.setAge(p.getAge());
        vo.setBirthday(p.getBirthday());
        vo.setProvinceCode(p.getProvinceCode());
        vo.setProvinceName(regionName(p.getProvinceCode()));
        vo.setCityCode(p.getCityCode());
        vo.setCityName(regionName(p.getCityCode()));
        vo.setDistrictCode(p.getDistrictCode());
        vo.setDistrictName(regionName(p.getDistrictCode()));
        vo.setProfession(p.getProfession());
        vo.setHeight(p.getHeight());
        vo.setEducation(p.getEducation());
        vo.setPersonality(JsonUtil.toStringList(p.getPersonality()));
        vo.setAppearances(JsonUtil.toStringList(p.getAppearances()));
        vo.setHobbies(JsonUtil.toStringList(p.getHobbies()));
        vo.setTags(JsonUtil.toStringList(p.getTags()));
        vo.setRequirementPreset(JsonUtil.toMap(p.getRequirementPreset()));
        vo.setRequirementCustom(JsonUtil.toMapList(p.getRequirementCustom()));
        vo.setMatchVisibility(user.getMatchVisibility() != null && user.getMatchVisibility() == 1);
        return vo;
    }

    private String regionName(String code) {
        if (code == null || code.isBlank()) return null;
        RegionEntity r = regionMapper.selectById(code);
        return r == null ? null : r.getName();
    }

    public ProfileVO updateProfile(Long userId, UpdateProfileDTO dto) {
        UserEntity user = userService.getById(userId);
        if (user == null) {
            throw new com.wait.server.exception.BusinessException(404, "user not found");
        }
        boolean userDirty = false;
        if (dto.getNickname() != null) { user.setNickname(dto.getNickname()); userDirty = true; }
        if (dto.getAvatar() != null)   { user.setAvatar(dto.getAvatar());     userDirty = true; }
        if (dto.getGender() != null)   { user.setGender(dto.getGender());     userDirty = true; }
        if (dto.getMatchVisibilityValue() != null) {
            user.setMatchVisibility(dto.getMatchVisibilityValue());
            userDirty = true;
        }
        if (userDirty) {
            userService.updateById(user);
        }

        UserProfileEntity p = findOrCreateByUserId(userId);
        if (dto.getRealName() != null)       p.setRealName(dto.getRealName());
        if (dto.getAge() != null)            p.setAge(dto.getAge());
        if (dto.getBirthday() != null)       p.setBirthday(dto.getBirthday());
        if (dto.getProvinceCode() != null)   p.setProvinceCode(dto.getProvinceCode());
        if (dto.getCityCode() != null)        p.setCityCode(dto.getCityCode());
        if (dto.getDistrictCode() != null)    p.setDistrictCode(dto.getDistrictCode());
        if (dto.getProfession() != null)     p.setProfession(dto.getProfession());
        if (dto.getHeight() != null)         p.setHeight(dto.getHeight());
        if (dto.getEducation() != null)      p.setEducation(dto.getEducation());
        if (dto.getPersonality() != null)   p.setPersonality(JsonUtil.toJson(dto.getPersonality()));
        if (dto.getAppearances() != null)    p.setAppearances(JsonUtil.toJson(dto.getAppearances()));
        if (dto.getHobbies() != null)        p.setHobbies(JsonUtil.toJson(dto.getHobbies()));
        if (dto.getTags() != null)           p.setTags(JsonUtil.toJson(dto.getTags()));
        if (dto.getRequirementPreset() != null)  p.setRequirementPreset(JsonUtil.toJson(dto.getRequirementPreset()));
        if (dto.getRequirementCustom() != null)   p.setRequirementCustom(JsonUtil.toJson(dto.getRequirementCustom()));
        updateById(p);

        UserEntity refresh = userService.getById(userId);
        refresh.setLastActiveAt(LocalDateTime.now());
        userService.updateById(refresh);

        return toVO(refresh, p);
    }
}

