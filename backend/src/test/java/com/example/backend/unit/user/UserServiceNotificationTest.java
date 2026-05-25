package com.example.backend.unit.user;

import com.example.backend.dto.NotificationSettingsRequest;
import com.example.backend.dto.NotificationSettingsResponse;
import com.example.backend.model.User;
import com.example.backend.model.UserSettings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceNotificationTest extends BaseUserTest {

    private static final String TEST_EMAIL = "mario.rossi@example.com";
    private static final Long TEST_USER_ID = 1L;

    // =========================================================================
    // TEST PER: showNotificationSettings
    // =========================================================================

    @Test
    @DisplayName("showNotificationSettings - Dovrebbe restituire le impostazioni reali se presenti nel DB")
    void showNotificationSettings_ShouldReturnExistingSettings() {
        // 1. SETUP
        User mockUser = new User();
        mockUser.setId(TEST_USER_ID);
        mockUser.setEmail(TEST_EMAIL);

        // Mockiamo l'azione interna di getUserFromSession(userId)
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(mockUser));

        // Impostazioni personalizzate salvate a DB (tutte disattivate per testare il
        // cambiamento)
        UserSettings existingSettings = new UserSettings(TEST_EMAIL, false, false, false);
        when(settingsRepository.findById(TEST_EMAIL)).thenReturn(Optional.of(existingSettings));

        // 2. EXECUTE
        NotificationSettingsResponse response = userService.showNotificationSettings(TEST_USER_ID);

        // 3. VERIFY
        assertNotNull(response);
        assertFalse(response.editEventEmailActivated());
        assertFalse(response.deleteEventEmailActivated());
        assertFalse(response.recomendedEventEmailActivated());

        verify(userRepository, times(1)).findById(TEST_USER_ID);
        verify(settingsRepository, times(1)).findById(TEST_EMAIL);
    }

    @Test
    @DisplayName("showNotificationSettings - Dovrebbe restituire il fallback (true, true, false) se l'utente non ha record a DB")
    void showNotificationSettings_ShouldReturnDefaultSettings_WhenUserHasNoRecord() {
        // 1. SETUP
        User mockUser = new User();
        mockUser.setId(TEST_USER_ID);
        mockUser.setEmail(TEST_EMAIL);

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(mockUser));

        // Il database restituisce Optional.empty() per questo utente pregresso
        when(settingsRepository.findById(TEST_EMAIL)).thenReturn(Optional.empty());

        // 2. EXECUTE
        NotificationSettingsResponse response = userService.showNotificationSettings(TEST_USER_ID);

        // 3. VERIFY
        assertNotNull(response);
        assertTrue(response.editEventEmailActivated(), "Dovrebbe essere true di default");
        assertTrue(response.deleteEventEmailActivated(), "Dovrebbe essere true di default");
        assertFalse(response.recomendedEventEmailActivated(), "Dovrebbe essere false di default nel tuo orElseGet");
    }

    // =========================================================================
    // TEST PER: modifySettings
    // =========================================================================

    @Test
    @DisplayName("modifySettings - Dovrebbe aggiornare e salvare correttamente le impostazioni fornite")
    void modifySettings_ShouldUpdateAndSaveSettings() {
        // 1. SETUP
        User mockUser = new User();
        mockUser.setId(TEST_USER_ID);
        mockUser.setEmail(TEST_EMAIL);

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(mockUser));

        // L'utente ha già dei settings memorizzati
        UserSettings initialSettings = new UserSettings(TEST_EMAIL, true, true, false);
        when(settingsRepository.findById(TEST_EMAIL)).thenReturn(Optional.of(initialSettings));

        // Prepariamo il pacchetto della richiesta DTO (Invertiamo i flag rispetto
        // all'inizio)
        NotificationSettingsRequest request = new NotificationSettingsRequest(false, false, true);

        // Configuriamo il mock per restituire l'oggetto catturato al salvataggio
        when(settingsRepository.save(any(UserSettings.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 2. EXECUTE
        NotificationSettingsResponse response = userService.modifySettings(request, TEST_USER_ID);

        // 3. VERIFY
        assertNotNull(response);
        assertFalse(response.editEventEmailActivated());
        assertFalse(response.deleteEventEmailActivated());
        assertTrue(response.recomendedEventEmailActivated());

        // Intercettiamo l'entità effettivamente passata al database per assicurarci che
        // i setter funzionino
        ArgumentCaptor<UserSettings> captor = ArgumentCaptor.forClass(UserSettings.class);
        verify(settingsRepository, times(1)).save(captor.capture());

        UserSettings savedEntity = captor.getValue();
        assertEquals(TEST_EMAIL, savedEntity.getEmail());
        assertFalse(savedEntity.isEditEventEmailActivated());
        assertFalse(savedEntity.isDeleteEventEmailActivated());
        assertTrue(savedEntity.isRecomendedEventEmailActivated());
    }
}