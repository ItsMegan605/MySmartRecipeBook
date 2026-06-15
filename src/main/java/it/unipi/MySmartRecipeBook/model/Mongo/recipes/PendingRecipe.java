package it.unipi.MySmartRecipeBook.model.Mongo.recipes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * Represents a lightweight, embedded sub-document stored within the Chef entity.
 * It tracks the essential details of a recipe that has been submitted by the chef
 * and is currently awaiting review and approval by the admin.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PendingRecipe {

    @Field("id")
    String id;

    private String title;

    @Field("image_url")
    private String imageURL;

    @Field("creation_date")
    private LocalDateTime creationDate;

}

