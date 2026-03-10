package it.unipi.MySmartRecipeBook.dto.recipe;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SliceRecipeDTO<T> {
    private List<T> content;
    private boolean hasNext;
    private boolean hasPrevious;
}
