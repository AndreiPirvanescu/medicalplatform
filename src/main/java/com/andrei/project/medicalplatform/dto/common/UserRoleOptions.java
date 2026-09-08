package com.andrei.project.medicalplatform.dto.common;

import java.util.List;

public record UserRoleOptions(
        List<UserOptionDto> users,
        boolean usedFallback
) {}
