package com.andrei.project.medicalplatform.dto.common;

import java.util.List;

/**
 * Result of looking up "users eligible for role X" (manager / doctor /
 * patient) for a form dropdown.
 *
 * usedFallback = true means no user actually matched that role (checked
 * case-insensitively, with or without a "ROLE_" prefix), so UserService
 * fell back to listing every user instead of handing the template an empty
 * list. Templates use this to show a small warning banner instead of just
 * silently displaying an unfiltered list, which is what was making the
 * "select a user" dropdowns look empty/broken - the query was fine, it
 * just never found a role-name match against your actual data.
 */
public record UserRoleOptions(
        List<UserOptionDto> users,
        boolean usedFallback
) {}
