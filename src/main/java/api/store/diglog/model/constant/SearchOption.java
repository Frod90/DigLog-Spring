package api.store.diglog.model.constant;

import api.store.diglog.common.exception.CustomException;
import api.store.diglog.common.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

import java.util.Arrays;

@AllArgsConstructor
public enum SearchOption {
    ALL("ALL"),
    TITLE("TITLE"),
    TAG("TAG");

    private final String option;

    @JsonCreator
    public static SearchOption fromOption(String optionString) {
        return Arrays.stream(SearchOption.values())
                .filter(searchOption -> searchOption.option.equals(optionString)).findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.POST_INVALID_SEARCH_OPTION));
    }

    @JsonValue
    public String getOption() {
        return option;
    }
}
