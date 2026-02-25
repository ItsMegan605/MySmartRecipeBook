package it.unipi.MySmartRecipeBook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class MySmartRecipeBookApplication {

	public static void main(String[] args) {
		SpringApplication.run(MySmartRecipeBookApplication.class, args);
	}

}
