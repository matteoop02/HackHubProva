package unicam.ids.HackHub.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ChronologicalDatesValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ChronologicalDates {
    String message() default "Le date non sono in ordine cronologico (Iscrizione <= Inizio <= Fine)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
