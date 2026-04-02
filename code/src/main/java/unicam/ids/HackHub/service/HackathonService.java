package unicam.ids.HackHub.service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import unicam.ids.HackHub.dto.requests.hackathon.CreateHackathonRequest;
import unicam.ids.HackHub.enums.HackathonState;
import unicam.ids.HackHub.model.Hackathon;
import unicam.ids.HackHub.model.User;
import unicam.ids.HackHub.repository.HackathonRepository;
import unicam.ids.HackHub.repository.UserRepository;

import unicam.ids.HackHub.dto.responses.HackathonResponse;
import unicam.ids.HackHub.exceptions.ResourceNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HackathonService {

    private final HackathonRepository hackathonRepository;
    private final UserRepository userRepository;

    public HackathonService(HackathonRepository hackathonRepository, UserRepository userRepository) {
        this.hackathonRepository = hackathonRepository;
        this.userRepository = userRepository;
    }

    public List<HackathonResponse> getHackathons(boolean isAuthenticated) {
        List<Hackathon> hackathons = isAuthenticated ? 
            hackathonRepository.findAll() : 
            hackathonRepository.findAllByIsPublic(true);

        return hackathons.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public HackathonResponse createHackathon(Authentication authentication, CreateHackathonRequest request) {
        User organizer = userRepository.findByUsernameAndIsDeletedFalse(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato o eliminato"));

        Hackathon hackathon = Hackathon.builder()
                .name(request.name())
                .place(request.place())
                .regulation(request.regulation())
                .subscriptionDeadline(request.subscriptionDeadline())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .reward(request.reward())
                .maxTeamSize(request.maxTeamSize())
                .isPublic(request.isPublic())
                .state(HackathonState.IN_ISCRIZIONE)
                .organizer(organizer)
                .build();

        return mapToResponse(hackathonRepository.save(hackathon));
    }

    private HackathonResponse mapToResponse(Hackathon hackathon) {
        return HackathonResponse.builder()
                .id(hackathon.getId())
                .name(hackathon.getName())
                .place(hackathon.getPlace())
                .regulation(hackathon.getRegulation())
                .subscriptionDeadline(hackathon.getSubscriptionDeadline())
                .startDate(hackathon.getStartDate())
                .endDate(hackathon.getEndDate())
                .reward(hackathon.getReward())
                .maxTeamSize(hackathon.getMaxTeamSize())
                .isPublic(hackathon.getIsPublic())
                .state(hackathon.getState())
                .organizerName(hackathon.getOrganizer() != null ? hackathon.getOrganizer().getName() + " " + hackathon.getOrganizer().getSurname() : "N/A")
                .build();
    }

    public void startHackathon(Authentication authentication, Long id) {
        Hackathon hackathon = hackathonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hackathon non trovato"));
        
        if (!hackathon.getOrganizer().getUsername().equals(authentication.getName())) {
            throw new unicam.ids.HackHub.exceptions.UnauthorizedAccessException("Solo l'organizzatore può avviare l'hackathon");
        }
        
        hackathon.start();
        hackathonRepository.save(hackathon);
    }

    public void closeHackathonSubscriptions(Authentication authentication, Long id) {
        Hackathon hackathon = hackathonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hackathon non trovato"));
        
        if (!hackathon.getOrganizer().getUsername().equals(authentication.getName())) {
            throw new unicam.ids.HackHub.exceptions.UnauthorizedAccessException("Solo l'organizzatore può chiudere le iscrizioni");
        }
        
        hackathon.closeSubscriptions();
        hackathonRepository.save(hackathon);
    }
}