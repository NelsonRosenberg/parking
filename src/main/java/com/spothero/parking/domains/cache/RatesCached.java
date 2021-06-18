package com.spothero.parking.domains.cache;

import com.spothero.parking.domains.rate.Rate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatesCached {

    @Builder.Default
    Map<String, Map<String, Set<Long>>> calculatedMap = new HashMap<>();

    @Builder.Default
    Map<Long, Rate> rateMap = new HashMap<>();

}
