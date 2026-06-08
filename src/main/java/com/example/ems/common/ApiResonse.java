package com.example.ems.common;

import lombok.Builder;

@Builder
public record ApiResonse<T>(Integer success,String message,T data) {
}
