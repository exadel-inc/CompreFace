package com.exadel.frs.entity;

import com.exadel.frs.enums.AppModelAccess;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = {"id", "guid"})
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
    @OneToMany(mappedBy = "model", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AppModel> appModelAccess;

    public void addAppModelAccess(App app, AppModelAccess access) {
        if (appModelAccess == null) {
            appModelAccess = new ArrayList<>();
        }
        if (app.getAppModelAccess() == null) {
            app.setAppModelAccess(new ArrayList<>());
        }
        AppModel appModel = new AppModel(app, this, access);
        appModelAccess.add(appModel);
        app.getAppModelAccess().add(appModel);
    }
}
