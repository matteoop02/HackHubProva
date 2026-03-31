package unicam.ids.HackHub.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import unicam.ids.HackHub.dto.requests.hackathon.CreateHackathonRequest;

public class ChronologicalDatesValidator implements ConstraintValidator<ChronologicalDates, CreateHackathonRequest> {

    @Override
    public boolean isValid(CreateHackathonRequest request, ConstraintValidatorContext context) {
        if (request == null || request.subscriptionDeadline() == null || request.startDate() == null || request.endDate() == null) {
            return true;
        }

        return !request.subscriptionDeadline().isAfter(request.startDate()) &&
               !request.startDate().isAfter(request.endDate());
    }
}
