package com.andrei.project.medicalplatform.controller;

import com.andrei.project.medicalplatform.dto.doctor.DoctorRegistrationDTO;
import com.andrei.project.medicalplatform.dto.doctor.DoctorResponseDTO;
import com.andrei.project.medicalplatform.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
@Tag(name = "Management Doctori", description = "Endpoint-uri securizate destinate Administratorului pentru gestionarea personalului medical")
public class DoctorController {

    private final DoctorService doctorService;

    @PostMapping
    @Operation(
            summary = "Adaugă un doctor nou",
            description = "Creează un cont de utilizator cu rolul DOCTOR și îi asociază detaliile medicale (licență, specializări)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Doctorul a fost creat cu succes",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = DoctorResponseDTO.class)) }),
            @ApiResponse(responseCode = "400", description = "Date de intrare invalide)", content = @Content),
            @ApiResponse(responseCode = "409", description = "Email duplicat ", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acces interzis - Lipsă drepturi de Administrator", content = @Content)
    })
    public ResponseEntity<DoctorResponseDTO> createDoctor(@Valid @RequestBody DoctorRegistrationDTO doctorRegistrationDto) {
        DoctorResponseDTO createdDoctor = doctorService.addDoctor(doctorRegistrationDto);
        return new ResponseEntity<>(createdDoctor, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(
            summary = "Vizualizează toți doctorii",
            description = "Returnează o listă completă cu toți doctorii înregistrați în platformă împreună cu datele lor de profil."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista a fost returnată cu succes",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = DoctorResponseDTO.class)) }),
            @ApiResponse(responseCode = "403", description = "Acces interzis", content = @Content)
    })
    public ResponseEntity<List<DoctorResponseDTO>> getAllDoctors() {
        List<DoctorResponseDTO> doctors = doctorService.getAllDoctors();
        return ResponseEntity.ok(doctors);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Vizualizează un doctor după ID",
            description = "Căutați detalii specifice ale unui singur doctor pe baza ID-ului unic din baza de date."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Doctorul a fost găsit",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = DoctorResponseDTO.class)) }),
            @ApiResponse(responseCode = "404", description = "Doctorul cu ID-ul specificat nu există", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acces interzis", content = @Content)
    })
    public ResponseEntity<DoctorResponseDTO> getDoctorById(@PathVariable Long id) {
        DoctorResponseDTO doctor = doctorService.getDoctorById(id);
        return ResponseEntity.ok(doctor);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Modifică datele unui doctor",
            description = "Actualizează informațiile utilizatorului (nume/prenume) și detaliile medicale (licență, specializări) pentru un ID existent."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Modificare realizată cu succes",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = DoctorResponseDTO.class)) }),
            @ApiResponse(responseCode = "400", description = "Datele trimise sunt invalide", content = @Content),
            @ApiResponse(responseCode = "404", description = "Doctorul cu ID-ul specificat nu există", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acces interzis", content = @Content) //TODO: De șters daca nu folosesc
    })
    public ResponseEntity<DoctorResponseDTO> updateDoctor(@PathVariable Long id, @Valid @RequestBody DoctorRegistrationDTO dto) {
        DoctorResponseDTO updatedDoctor = doctorService.updateDoctor(id, dto);
        return ResponseEntity.ok(updatedDoctor);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Șterge un doctor",
            description = "Elimină definitiv un doctor din sistem, eliminând în cascadă și utilizatorul (User) asociat acestuia."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Doctorul a fost șters cu succes, nu returnează conținut", content = @Content),
            @ApiResponse(responseCode = "404", description = "Doctorul cu ID-ul specificat nu există", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acces interzis", content = @Content)
    })
    public ResponseEntity<Void> deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return ResponseEntity.noContent().build();
    }
}