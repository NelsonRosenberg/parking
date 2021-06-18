package com.spothero.parking.domains.rate;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedEntityGraph;
import javax.persistence.SequenceGenerator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@NamedEntityGraph(
        name = "rate.default",
        includeAllAttributes = true)
public class Rate {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RateSeq")
    @SequenceGenerator(name = "RateSeq", sequenceName = "rate_seq")
    private Long id;

    @Column
    @Builder.Default
    @ElementCollection(fetch = FetchType.LAZY)
    private List<String> days = new ArrayList<>();

    @Column
    private String startTime;

    @Column
    private String endTime;

    @Column
    private String tz;

    @Column
    private Integer price;
}
