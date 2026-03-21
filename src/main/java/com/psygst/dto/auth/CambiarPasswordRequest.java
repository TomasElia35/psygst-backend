package com.psygst.dto.auth;

public record CambiarPasswordRequest(String oldPassword, String newPassword) {
}
