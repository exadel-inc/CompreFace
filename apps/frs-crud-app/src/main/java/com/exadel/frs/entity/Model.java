package com.exadel.frs.entity;

import com.exadel.frs.enums.AppModelAccess;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = {"guid"})
public class Model {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "model_id_seq")
    @SequenceGenerator(name = "model_id_seq", sequenceName = "model_id_seq", allocationSize = 1)
    private Long id;
    private String name;
    private String guid;
    private String apiKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_id")
    private App app;

    @ToString.Exclude
    @Builder.Default
    @OneToMany(mappedBy = "model", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AppModel> appModelAccess = new ArrayList<>();

    public void addAppModelAccess(App app, AppModelAccess access) {
        AppModel appModel = new AppModel(app, this, access);
        appModelAccess.add(appModel);
        app.getAppModelAccess().add(appModel);
    }

    public Optional<AppModel> getAppModel(String appGuid) {
        return appModelAccess
                .stream()
                .filter(appModel -> appModel.getApp().getGuid().equals(appGuid))
                .findFirst();
    }

}
