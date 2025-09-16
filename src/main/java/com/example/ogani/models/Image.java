package com.example.ogani.models;

import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "image")
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    private String type;

    private long size;

    @Lob
    private byte[] data;

    @ManyToOne
    @JoinColumn(name = "uploaded_by")
    private User uploadedBy;

}
