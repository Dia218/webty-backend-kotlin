package org.team14.webty.voting.dto;

import jakarta.validation.constraints.NotNull;

public record SimilarRequest(@NotNull Long targetWebtoonId, @NotNull Long choiceWebtoonId) {
}