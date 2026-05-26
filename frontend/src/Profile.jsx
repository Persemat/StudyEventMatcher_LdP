import { useEffect, useState, useMemo } from "react";
import { 
  Bell, 
  ChevronRight, 
  Lock, 
  LogOut, 
  Mail, 
  Save, 
  Settings, 
  User, 
  MapPin, 
  Tags    
} from "lucide-react";
import { useNavigate } from "react-router-dom";

// Assicurati che il percorso di importazione sia corretto in base alla tua alberatura
import { loadCityOptions, loadTagOptions } from "./catalogs"; 

export default function Profile({ user, onLogout, onUserUpdated }) {
  const navigate = useNavigate();

  const [personalForm, setPersonalForm] = useState({ name: "", lastname: "" });
  const [emailForm, setEmailForm] = useState({ email: "" });
  const [passwordForm, setPasswordForm] = useState({
    currentPassword: "",
    newPassword: "",
    confirmPassword: "",
  });

  // Stato globale per le preferenze (verrà inviato tutto insieme)
  const [preferencesForm, setPreferencesForm] = useState({
    favouriteCity: "",
    macroTags: [],
    microTags: []
  });

  // NUOVO STATO: Preferenze notifiche (tabella UserSettings separata)
  const [notificationsForm, setNotificationsForm] = useState({
    editEventEmailActivated: true,
    deleteEventEmailActivated: true,
    recomendedEventEmailActivated: true,
  });

  // Stati per i cataloghi
  const [cityOptions, setCityOptions] = useState([]);
  const [tagOptions, setTagOptions] = useState([]);

  const [personalErrors, setPersonalErrors] = useState({});
  const [emailErrors, setEmailErrors] = useState({});
  const [passwordErrors, setPasswordErrors] = useState({});
  const [preferencesErrors, setPreferencesErrors] = useState({}); 
  const [messages, setMessages] = useState({});
  const [loading, setLoading] = useState("");
  
  const [activeSection, setActiveSection] = useState("");
  const [activeEditArea, setActiveEditArea] = useState("");
  const [isCityFocused, setIsCityFocused] = useState(false);

  // Caricamento dei cataloghi al mount
  useEffect(() => {
    loadCityOptions().then(setCityOptions).catch(console.error);
    loadTagOptions().then(setTagOptions).catch(console.error);
  }, []);

  useEffect(() => {
    if (!user) return;

    setPersonalForm({
      name: user.name ?? "",
      lastname: user.lastname ?? "",
    });
    setEmailForm({ email: user.email ?? "" });
    
    // Popola i dati delle preferenze appena l'utente viene caricato/aggiornato
    setPreferencesForm({
      favouriteCity: user.favouriteCity ?? "",
      macroTags: user.macroTags ?? [],
      microTags: user.microTags ?? []
    });

    const fetchNotificationSettings = async () => {
      try {
        const response = await fetch("/api/user/me/settings", { credentials: "include" });
        if (response.ok) {
          const settings = await response.json();
          setNotificationsForm({
            editEventEmailActivated: settings.editEventEmailActivated,
            deleteEventEmailActivated: settings.deleteEventEmailActivated,
            recomendedEventEmailActivated: settings.recomendedEventEmailActivated,
          });
        }
      } catch (err) {
        console.error("Errore recupero notifiche:", err);
      }
    };
    fetchNotificationSettings();
  }, [user]);

  const filteredCities = useMemo(() => {
    const query = preferencesForm.favouriteCity.trim().toLowerCase();
    if (!query || query.length < 2) return []; // Mostra suggerimenti solo dopo 2 caratteri
    return cityOptions
      .filter((option) => option.label.toLowerCase().includes(query))
      .slice(0, 10); // Limita a 10 risultati per non coprire tutto lo schermo
  }, [preferencesForm.favouriteCity, cityOptions]);

  const handleLogoutClick = async () => {
    await onLogout();
    navigate("/login");
  };

  const resetCardState = (card) => {
    setMessages((prev) => ({ ...prev, [card]: "" }));

    if (card === "personal") setPersonalErrors({});
    if (card === "email") setEmailErrors({});
    if (card === "password") setPasswordErrors({});
    if (["city", "macro", "micro"].includes(card)) setPreferencesErrors({}); 
  };

  const readError = async (response) => {
    const contentType = response.headers.get("content-type") ?? "";

    if (contentType.includes("application/json")) {
      const data = await response.json();
      return data.errore ?? data.message ?? "Errore durante il salvataggio";
    }

    return response.text();
  };

  const savePersonalData = async (event) => {
    event.preventDefault();
    resetCardState("personal");

    const errors = {};
    if (!personalForm.name.trim()) errors.name = "Nome obbligatorio";
    if (!personalForm.lastname.trim()) errors.lastname = "Cognome obbligatorio";

    if (Object.keys(errors).length > 0) {
      setPersonalErrors(errors);
      setMessages((prev) => ({
        ...prev,
        personal: "Campi obbligatori mancanti",
      }));
      return;
    }

    setLoading("personal");

    try {
      const response = await fetch("/api/user/me/personal-data", {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify({
          name: personalForm.name.trim(),
          lastname: personalForm.lastname.trim(),
        }),
      });

      if (!response.ok) throw new Error(await readError(response));

      const updatedUser = await response.json();
      onUserUpdated(updatedUser);
      setMessages((prev) => ({
        ...prev,
        personal: "Dati personali aggiornati con successo",
      }));

      setTimeout(() => {
        setMessages((prev) => ({ ...prev, personal: "" }));
      }, 2000);

    } catch (error) {
      setMessages((prev) => ({ ...prev, personal: error.message }));
    } finally {
      setLoading("");
    }
  };

  const saveEmail = async (event) => {
    event.preventDefault();
    resetCardState("email");

    const email = emailForm.email.trim();
    const errors = {};

    if (!email) errors.email = "Email obbligatoria";
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      errors.email = "Email non valida";
    }

    if (Object.keys(errors).length > 0) {
      setEmailErrors(errors);
      return;
    }

    setLoading("email");

    try {
      const response = await fetch("/api/user/me/email", {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify({ email }),
      });

      if (!response.ok) throw new Error(await readError(response));

      const updatedUser = await response.json();
      onUserUpdated(updatedUser);
      setMessages((prev) => ({
        ...prev,
        email: "Email aggiornata con successo",
      }));

      setTimeout(() => {
        setMessages((prev) => ({ ...prev, email: "" }));
      }, 2000);

    } catch (error) {
      setEmailErrors({ email: error.message });
    } finally {
      setLoading("");
    }
  };

  const savePassword = async (event) => {
    event.preventDefault();
    resetCardState("password");

    setLoading("password");

    try {
      const response = await fetch("/api/user/me/password", {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify({
          currentPassword: passwordForm.currentPassword,
          newPassword: passwordForm.newPassword,
          confirmPassword: passwordForm.confirmPassword,
        }),
      });

      if (!response.ok) throw new Error(await readError(response));

      const updatedUser = await response.json();
      onUserUpdated(updatedUser);
      setPasswordForm({
        currentPassword: "",
        newPassword: "",
        confirmPassword: "",
      });
      setMessages((prev) => ({
        ...prev,
        password: "Password aggiornata con successo",
      }));

      setTimeout(() => {
        setMessages((prev) => ({ ...prev, password: "" }));
      }, 2000);

    } catch (error) {
      if (error.message === "Password attuale non corretta" || error.message === "Password attuale obbligatoria") {
        setPasswordErrors({ currentPassword: error.message });
      } else if (error.message === "Conferma password obbligatoria" || error.message === "Le password non corrispondono") {
        setPasswordErrors({ confirmPassword: error.message });
      } else {
        setPasswordErrors({ newPassword: error.message });
      }
    } finally {
      setLoading("");
    }
  };

  // Funzione unica per gestire il salvataggio delle preferenze (Città, Macro e Micro)
  const savePreferences = async (event, area) => {
    event.preventDefault();
    resetCardState(area);

    const errors = {};
    
    // Validazione specifica in base all'area attiva
    if (area === "city") {
      const currentCity = preferencesForm.favouriteCity.trim();
      
      const isValidCity = cityOptions.some(city => city.label === currentCity);

      if (!currentCity) {
        errors.favouriteCity = "Bisogna indicare una città preferita";
      } else if (!isValidCity) {
        errors.favouriteCity = "Seleziona una città valida dall'elenco suggerito";
      }
    }
    if (area === "macro") {
      if (preferencesForm.macroTags.length === 0) errors.macroTags = "Almeno una macro area di interesse deve essere indicata";
      if (preferencesForm.macroTags.length > 3) errors.macroTags = "Si possono indicare al massimo 3 aree di interesse generale";
    }
    if (area === "micro" && preferencesForm.microTags.length > 5) {
      errors.microTags = "Si possono indicare al massimo 5 argomenti di interesse specifici";
    }

    if (Object.keys(errors).length > 0) {
      setPreferencesErrors(errors);
      return;
    }

    setLoading(area);

    try {
      // Endpoint unico: mando sempre il pacchetto completo per non far scattare validazioni nulle backend
      const response = await fetch("/api/user/me/preferences", {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify({
          favouriteCity: preferencesForm.favouriteCity,
          macroTags: preferencesForm.macroTags,
          microTags: preferencesForm.microTags,
        }),
      });

      if (!response.ok) throw new Error(await readError(response));

      const updatedUser = await response.json();
      onUserUpdated(updatedUser);
      setMessages((prev) => ({
        ...prev,
        [area]: "Preferenze aggiornate con successo",
      }));

      setTimeout(() => {
        setMessages((prev) => ({
          ...prev,
          [area]: "", 
        }));
      }, 2000);

    } catch (error) {
      setPreferencesErrors({ [area]: error.message });
    } finally {
      setLoading("");
    }
  };

  // NUOVA FUNZIONE: Salvataggio notifiche tramite Service
  const saveNotifications = async (event) => {
    event.preventDefault();
    setMessages((prev) => ({ ...prev, notifications: "" }));
    setLoading("notifications");

    try {
      const response = await fetch("/api/user/me/settings", {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify(notificationsForm),
      });

      if (!response.ok) throw new Error(await readError(response));

      setMessages((prev) => ({
        ...prev,
        notifications: "Impostazioni notifiche aggiornate con successo",
      }));

      setTimeout(() => {
        setMessages((prev) => ({ ...prev, notifications: "" }));
      }, 2000);

    } catch (error) {
      setMessages((prev) => ({ ...prev, notifications: error.message }));
    } finally {
      setLoading("");
    }
  };

  if (!user) return null;

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Il Mio Profilo</h1>
          <p className="text-gray-600 mt-1">
            Visualizza e modifica le tue informazioni personali
          </p>
        </div>

        <button
          onClick={handleLogoutClick}
          className="bg-red-600 text-white px-6 py-3 rounded-lg font-medium hover:bg-red-700 flex items-center justify-center gap-2"
        >
          <LogOut className="w-5 h-5" />
          Logout
        </button>
      </div>

      <div className="bg-white rounded-lg shadow overflow-hidden">
        <div className="p-8 border-b border-gray-200 bg-gray-50/50">
          <div className="flex flex-col gap-6 sm:flex-row sm:items-center">
            <div className="w-24 h-24 bg-blue-100 rounded-full flex items-center justify-center border-4 border-white shadow-sm">
              <User className="w-12 h-12 text-blue-600" />
            </div>

            <div>
              <h2 className="text-2xl font-bold text-gray-900">
                {user.name} {user.lastname}
              </h2>
              <p className="text-gray-600 font-medium">{user.email}</p>
            </div>
          </div>
        </div>

        <div className="border-b border-gray-200 px-4 py-3 sm:px-8">
          <div className="flex flex-col gap-2 md:flex-row">
            
            {/* Tasto Modifica Preferenze Sbloccato */}
            <button
              type="button"
              onClick={() => {
                if (activeSection === "preferences") {
                  setActiveSection("");
                  setActiveEditArea("");
                  return;
                }
                setActiveSection("preferences");
                setActiveEditArea("");
              }}
              className={`flex items-center justify-center gap-2 rounded-lg px-4 py-3 text-sm font-semibold transition ${
                activeSection === "preferences"
                  ? "bg-blue-600 text-white"
                  : "border border-gray-200 text-gray-700 hover:bg-gray-50"
              }`}
            >
              <Settings className="w-4 h-4" />
              Modifica preferenze
            </button>

            <button
              type="button"
              onClick={() => {
                if (activeSection === "data") {
                  setActiveSection("");
                  setActiveEditArea("");
                  return;
                }
                setActiveSection("data");
                setActiveEditArea("");
              }}
              className={`flex items-center justify-center gap-2 rounded-lg px-4 py-3 text-sm font-semibold transition ${
                activeSection === "data"
                  ? "bg-blue-600 text-white"
                  : "border border-gray-200 text-gray-700 hover:bg-gray-50"
              }`}
            >
              <User className="w-4 h-4" />
              Modifica dati
            </button>

            <button
              type="button"
              onClick={() => {
                if (activeSection === "notifications") {
                  setActiveSection("");
                  return;
                }
                setActiveSection("notifications");
              }}
              className={`flex items-center justify-center gap-2 rounded-lg px-4 py-3 text-sm font-semibold transition ${
                activeSection === "notifications"
                  ? "bg-blue-600 text-white"
                  : "border border-gray-200 text-gray-700 hover:bg-gray-50"
              }`}
            >
              <Bell className="w-4 h-4" />
              Impostazioni notifiche
            </button>
          </div>
        </div>

        <div className="p-6 sm:p-8">
          
          {/* SEZIONE: MODIFICA DATI (Rimasta invariata per i campi Name, Email, Password) */}
          {activeSection === "data" && (
            <div className="space-y-6">
              <div className="grid grid-cols-1 gap-3 md:grid-cols-3">
                <EditAreaButton
                  icon={<User className="h-5 w-5" />}
                  label="Nome e cognome"
                  active={activeEditArea === "personal"}
                  onClick={() => setActiveEditArea((c) => c === "personal" ? "" : "personal")}
                />
                <EditAreaButton
                  icon={<Mail className="h-5 w-5" />}
                  label="Email"
                  active={activeEditArea === "email"}
                  onClick={() => setActiveEditArea((c) => c === "email" ? "" : "email")}
                />
                <EditAreaButton
                  icon={<Lock className="h-5 w-5" />}
                  label="Password"
                  active={activeEditArea === "password"}
                  onClick={() => setActiveEditArea((c) => c === "password" ? "" : "password")}
                />
              </div>

              {activeEditArea === "personal" && (
                <form onSubmit={savePersonalData} className="rounded-lg border border-gray-200 p-5">
                  <div className="mb-5 flex items-center gap-3">
                    <div className="rounded-lg bg-blue-50 p-3">
                      <User className="h-5 w-5 text-blue-600" />
                    </div>
                    <div>
                      <h3 className="font-bold text-gray-900">Nome e cognome</h3>
                      <p className="text-sm text-gray-500">Aggiorna dati anagrafici</p>
                    </div>
                  </div>
                  <div className="space-y-4">
                    <Field id="name" label="Nome" value={personalForm.name} error={personalErrors.name} onChange={(val) => setPersonalForm((p) => ({ ...p, name: val }))} />
                    <Field id="lastname" label="Cognome" value={personalForm.lastname} error={personalErrors.lastname} onChange={(val) => setPersonalForm((p) => ({ ...p, lastname: val }))} />
                  </div>
                  <CardMessage message={messages.personal} />
                  <button type="submit" disabled={loading === "personal"} className="mt-5 flex w-full items-center justify-center gap-2 rounded-lg bg-blue-600 px-4 py-3 font-semibold text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-70">
                    <Save className="h-4 w-4" />
                    {loading === "personal" ? "Salvataggio..." : "Salva dati"}
                  </button>
                </form>
              )}

              {activeEditArea === "email" && (
                <form onSubmit={saveEmail} className="rounded-lg border border-gray-200 p-5">
                  <div className="mb-5 flex items-center gap-3">
                    <div className="rounded-lg bg-blue-50 p-3">
                      <Mail className="h-5 w-5 text-blue-600" />
                    </div>
                    <div>
                      <h3 className="font-bold text-gray-900">Email</h3>
                      <p className="text-sm text-gray-500">Cambia indirizzo email</p>
                    </div>
                  </div>
                  <Field id="email" type="email" label="Email" value={emailForm.email} error={emailErrors.email} onChange={(val) => setEmailForm({ email: val })} />
                  <CardMessage message={messages.email} />
                  <button type="submit" disabled={loading === "email"} className="mt-5 flex w-full items-center justify-center gap-2 rounded-lg bg-blue-600 px-4 py-3 font-semibold text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-70">
                    <Save className="h-4 w-4" />
                    {loading === "email" ? "Salvataggio..." : "Salva email"}
                  </button>
                </form>
              )}

              {activeEditArea === "password" && (
                <form onSubmit={savePassword} className="rounded-lg border border-gray-200 p-5">
                  <div className="mb-5 flex items-center gap-3">
                    <div className="rounded-lg bg-blue-50 p-3">
                      <Lock className="h-5 w-5 text-blue-600" />
                    </div>
                    <div>
                      <h3 className="font-bold text-gray-900">Password</h3>
                      <p className="text-sm text-gray-500">Aggiorna credenziali</p>
                    </div>
                  </div>
                  <div className="space-y-4">
                    <Field id="currentPassword" type="password" label="Password attuale" value={passwordForm.currentPassword} error={passwordErrors.currentPassword} onChange={(val) => setPasswordForm((p) => ({ ...p, currentPassword: val }))} />
                    <Field id="newPassword" type="password" label="Nuova password" value={passwordForm.newPassword} error={passwordErrors.newPassword} onChange={(val) => setPasswordForm((p) => ({ ...p, newPassword: val }))} />
                    <Field id="confirmPassword" type="password" label="Conferma nuova password" value={passwordForm.confirmPassword} error={passwordErrors.confirmPassword} onChange={(val) => setPasswordForm((p) => ({ ...p, confirmPassword: val }))} />
                  </div>
                  <CardMessage message={messages.password} />
                  <button type="submit" disabled={loading === "password"} className="mt-5 flex w-full items-center justify-center gap-2 rounded-lg bg-blue-600 px-4 py-3 font-semibold text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-70">
                    <Save className="h-4 w-4" />
                    {loading === "password" ? "Salvataggio..." : "Aggiorna password"}
                  </button>
                </form>
              )}
            </div>
          )}

          {/* SEZIONE: MODIFICA PREFERENZE DIVISA IN TRE FORM */}
          {activeSection === "preferences" && (
            <div className="space-y-6">
              <div className="grid grid-cols-1 gap-3 md:grid-cols-3">
                <EditAreaButton
                  icon={<MapPin className="h-5 w-5" />}
                  label="Città preferita"
                  active={activeEditArea === "city"}
                  onClick={() => setActiveEditArea((c) => c === "city" ? "" : "city")}
                />
                <EditAreaButton
                  icon={<Tags className="h-5 w-5" />}
                  label="Macro aree di interesse"
                  active={activeEditArea === "macro"}
                  onClick={() => setActiveEditArea((c) => c === "macro" ? "" : "macro")}
                />
                <EditAreaButton
                  icon={<Tags className="h-5 w-5" />}
                  label="Argomenti specifici"
                  active={activeEditArea === "micro"}
                  onClick={() => setActiveEditArea((c) => c === "micro" ? "" : "micro")}
                />
              </div>

              {/* FORM 1: CITTÀ PREFERITA */}
              {activeEditArea === "city" && (
                <form onSubmit={(e) => savePreferences(e, "city")} className="rounded-lg border border-gray-200 p-5">
                  <div className="relative">
                    <label className="mb-2 block text-sm font-semibold text-gray-700">Città Preferita *</label>
                    <input
                      type="text"
                      value={preferencesForm.favouriteCity}
                      onChange={(e) => setPreferencesForm(p => ({ ...p, favouriteCity: e.target.value }))}
                      onFocus={() => setIsCityFocused(true)}
                      // Il timeout serve a far registrare il click sulla lista prima che sparisca
                      onBlur={() => {
                        // Aspettiamo che il click sul dropdown venga registrato
                        setTimeout(() => {
                          setIsCityFocused(false);
                          
                          // LOGICA EXTRA: Se l'utente esce e quello che ha scritto non è valido, 
                          // potresti voler resettare il campo al valore originale (quello di user.favouriteCity)
                          const isValid = cityOptions.some(city => city.label === preferencesForm.favouriteCity);
                          if (!isValid && preferencesForm.favouriteCity !== "") {
                              setPreferencesForm(p => ({ ...p, favouriteCity: user.favouriteCity }));
                              
                          }
                        }, 200);
                      }}
                      placeholder="Inizia a scrivere il nome della città..."
                      className="w-full rounded-lg border border-gray-300 px-4 py-3 outline-none focus:border-blue-500"
                    />
                    
                    {/* Dropdown Personalizzato */}
                    {isCityFocused && filteredCities.length > 0 && (
                      <ul className="absolute z-20 w-full mt-1 max-h-48 overflow-y-auto rounded-lg border border-gray-200 bg-white shadow-xl list-none p-0 m-0">
                        {filteredCities.map(c => (
                          <li 
                            key={c.label} 
                            onMouseDown={() => setPreferencesForm(p => ({ ...p, favouriteCity: c.label }))} 
                            className="px-4 py-3 cursor-pointer text-sm hover:bg-blue-50 border-b last:border-0"
                          >
                            {c.label}
                          </li>
                        ))}
                      </ul>
                    )}
                  </div>
                  <CardMessage message={messages.city} />
                  <button type="submit" disabled={loading === "city"} className="mt-5 w-full bg-blue-600 text-white py-3 rounded-lg font-bold hover:bg-blue-700">
                    {loading === "city" ? "Salvataggio..." : "Salva Città"}
                  </button>
                </form>
              )}

              {/* FORM 2: MACRO AREE */}
              {activeEditArea === "macro" && (
                <form onSubmit={(e) => savePreferences(e, "macro")} className="rounded-lg border border-gray-200 p-5">
                  <div className="mb-5 flex items-center gap-3">
                    <div className="rounded-lg bg-blue-50 p-3">
                      <Tags className="h-5 w-5 text-blue-600" />
                    </div>
                    <div>
                      <h3 className="font-bold text-gray-900">Macro aree di interesse</h3>
                      <p className="text-sm text-gray-500">Scegli le categorie generali (max 3)</p>
                    </div>
                  </div>

                  <TagSelectBox
                    title="Inserisci almeno un'area di interesse"
                    limit={3}
                    selectedTags={preferencesForm.macroTags}
                    availableTags={tagOptions.filter(t => t.isMacro)}
                    isMacro={true}
                    colorScheme="blue"
                    placeholder="Cerca area..."
                    onAdd={(tag) => setPreferencesForm(prev => ({...prev, macroTags: [...prev.macroTags, tag]}))}
                    onRemove={(tag) => {
                       setPreferencesForm(prev => {
                         const newMacros = prev.macroTags.filter(t => t !== tag);
                         // Pulizia a cascata: se elimino un macro, elimino anche i micro tag associati a lui
                         const microTagsToRemove = tagOptions.filter(t => !t.isMacro && t.macroTag === tag).map(t => t.microTag);
                         const newMicros = prev.microTags.filter(t => !microTagsToRemove.includes(t));
                         return { ...prev, macroTags: newMacros, microTags: newMicros };
                       });
                    }}
                    error={preferencesErrors.macroTags || preferencesErrors.macro}
                  />

                  <CardMessage message={messages.macro} />

                  <button
                    type="submit"
                    disabled={loading === "macro"}
                    className="mt-5 flex w-full items-center justify-center gap-2 rounded-lg bg-blue-600 px-4 py-3 font-semibold text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-70"
                  >
                    <Save className="h-4 w-4" />
                    {loading === "macro" ? "Salvataggio..." : "Salva macro aree"}
                  </button>
                </form>
              )}

              {/* FORM 3: MICRO AREE */}
              {activeEditArea === "micro" && (
                <form onSubmit={(e) => savePreferences(e, "micro")} className="rounded-lg border border-gray-200 p-5">
                  <div className="mb-5 flex items-center gap-3">
                    <div className="rounded-lg bg-blue-50 p-3">
                      <Tags className="h-5 w-5 text-blue-600" />
                    </div>
                    <div>
                      <h3 className="font-bold text-gray-900">Argomenti specifici</h3>
                      <p className="text-sm text-gray-500">Dettaglia i tuoi interessi (max 5)</p>
                    </div>
                  </div>

                  {preferencesForm.macroTags.length === 0 ? (
                     <div className="rounded-lg bg-yellow-50 p-4 text-sm text-yellow-800 border border-yellow-200">
                       Seleziona e salva prima almeno una macro area di interesse per poter scegliere gli argomenti specifici.
                     </div>
                  ) : (
                    <TagSelectBox
                      title="Scegli fino a 5 argomenti di interesse specifico"
                      limit={5}
                      selectedTags={preferencesForm.microTags}
                      availableTags={tagOptions.filter(t => !t.isMacro && preferencesForm.macroTags.includes(t.macroTag))}
                      isMacro={false}
                      colorScheme="green"
                      placeholder="Cerca competenza..."
                      onAdd={(tag) => setPreferencesForm(prev => ({...prev, microTags: [...prev.microTags, tag]}))}
                      onRemove={(tag) => setPreferencesForm(prev => ({...prev, microTags: prev.microTags.filter(t => t !== tag)}))}
                      error={preferencesErrors.microTags || preferencesErrors.micro}
                    />
                  )}

                  <CardMessage message={messages.micro} />

                  <button
                    type="submit"
                    disabled={loading === "micro" || preferencesForm.macroTags.length === 0}
                    className="mt-5 flex w-full items-center justify-center gap-2 rounded-lg bg-blue-600 px-4 py-3 font-semibold text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-70"
                  >
                    <Save className="h-4 w-4" />
                    {loading === "micro" ? "Salvataggio..." : "Salva argomenti specifici"}
                  </button>
                </form>
              )}
            </div>
          )}


          {/* SEZIONE: IMPOSTAZIONI NOTIFICHE */}
          {activeSection === "notifications" && (
            <form onSubmit={saveNotifications} className="rounded-lg border border-gray-200 p-6 space-y-6">
              <div className="flex items-center gap-3 mb-4">
                <div className="rounded-lg bg-blue-50 p-3">
                  <Bell className="h-5 w-5 text-blue-600" />
                </div>
                <div>
                  <h3 className="font-bold text-gray-900">Notifiche Email</h3>
                  <p className="text-sm text-gray-500">Scegli quali comunicazioni ricevere</p>
                </div>
              </div>

              <div className="space-y-4">
                <div className="flex items-start gap-3">
                  <input
                    id="editEventEmailActivated"
                    type="checkbox"
                    checked={notificationsForm.editEventEmailActivated}
                    onChange={(e) => setNotificationsForm(p => ({ ...p, editEventEmailActivated: e.target.checked }))}
                    className="mt-1 h-4 w-4 rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                  />
                  <label htmlFor="editEventEmailActivated" className="text-sm text-gray-700 font-medium">
                    Ricevi un'email quando un evento a cui partecipi viene modificato
                  </label>
                </div>

                <div className="flex items-start gap-3">
                  <input
                    id="deleteEventEmailActivated"
                    type="checkbox"
                    checked={notificationsForm.deleteEventEmailActivated}
                    onChange={(e) => setNotificationsForm(p => ({ ...p, deleteEventEmailActivated: e.target.checked }))}
                    className="mt-1 h-4 w-4 rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                  />
                  <label htmlFor="deleteEventEmailActivated" className="text-sm text-gray-700 font-medium">
                    Ricevi un'email quando un evento a cui partecipi viene cancellato
                  </label>
                </div>

                <div className="flex items-start gap-3">
                  <input
                    id="recomendedEventEmailActivated"
                    type="checkbox"
                    checked={notificationsForm.recomendedEventEmailActivated}
                    onChange={(e) => setNotificationsForm(p => ({ ...p, recomendedEventEmailActivated: e.target.checked }))}
                    className="mt-1 h-4 w-4 rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                  />
                  <label htmlFor="recomendedEventEmailActivated" className="text-sm text-gray-700 font-medium">
                    Ricevi email di raccomandazione per nuovi eventi basati sui tuoi interessi
                  </label>
                </div>
              </div>

              <CardMessage message={messages.notifications} />

              <button
                type="submit"
                disabled={loading === "notifications"}
                className="flex w-full items-center justify-center gap-2 rounded-lg bg-blue-600 px-4 py-3 font-semibold text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-70"
              >
                <Save className="h-4 w-4" />
                {loading === "notifications" ? "Salvataggio..." : "Salva impostazioni"}
              </button>
            </form>
          )}
        </div>
      </div>
    </div>
  );
}

// ---------------------------
// COMPONENTI DI SUPPORTO UI
// ---------------------------

// NUOVO COMPONENTE: Ricrea l'interfaccia a Pills in base agli screenshot forniti
function TagSelectBox({ title, limit, selectedTags, availableTags, isMacro, colorScheme, placeholder, error, onAdd, onRemove }) {
  const [inputValue, setInputValue] = useState("");
  const [isFocused, setIsFocused] = useState(false);
  const isFull = selectedTags.length >= limit;
  const isBlue = colorScheme === "blue";

  const handleKeyDown = (e) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      if (!inputValue.trim()) return;
      
      const matched = availableTags.find(t => 
        t.label.toLowerCase() === inputValue.toLowerCase() || 
        (isMacro ? t.macroTag.toLowerCase() === inputValue.toLowerCase() : t.microTag.toLowerCase() === inputValue.toLowerCase())
      );

      if (matched) {
        const tagToSave = isMacro ? matched.macroTag : matched.microTag;
        if (!selectedTags.includes(tagToSave) && !isFull) {
          onAdd(tagToSave);
          setInputValue(""); 
        }
      }
    }
  };

  // Filtriamo i suggerimenti in base a ciò che l'utente digita
  const suggestions = availableTags.filter(t => {
    const isAlreadySelected = selectedTags.includes(isMacro ? t.macroTag : t.microTag);
    if (isAlreadySelected) return false;
    
    if (!inputValue.trim()) return true;
    
    const searchTarget = isMacro ? t.macroTag.toLowerCase() : t.microTag.toLowerCase();
    return searchTarget.includes(inputValue.toLowerCase());
  });

  return (
    <div className="mb-4 relative">
      <div className="flex justify-between items-center mb-2">
        <label className="text-sm font-semibold text-gray-700">{title}</label>
        <span className={`text-xs font-bold ${isFull ? 'text-red-600' : 'text-gray-500'}`}>
          {selectedTags.length} / {limit}
        </span>
      </div>
      
      {/* Contenitore principale Tags + Input */}
      <div className={`flex flex-wrap items-center gap-2 rounded-lg border p-3 min-h-[52px] transition-colors ${
          error ? 'border-red-500 bg-red-50' : 
          isFull ? 'border-gray-300 bg-gray-200/50' : 'border-gray-300 bg-gray-50'
        }`}
      >
        {/* Renderizza i "Pills" */}
        {selectedTags.map(tag => (
          <span key={tag} className={`inline-flex items-center gap-1.5 rounded-full px-3 py-1 text-sm font-medium shadow-sm ${isBlue ? 'bg-blue-600 text-white' : 'bg-emerald-500 text-white'}`}>
            {tag}
            <button 
              type="button" 
              onClick={() => onRemove(tag)} 
              className="ml-1 hover:text-gray-200 focus:outline-none flex items-center justify-center leading-none text-base"
            >
              &times;
            </button>
          </span>
        ))}

        {/* Input Textuale */}
        <div className="relative flex-1 min-w-[150px]">
          <input
            type="text"
            value={inputValue}
            onChange={(e) => {
              setInputValue(e.target.value);
              setIsFocused(true);
            }}
            onFocus={() => setIsFocused(true)}
            onBlur={() => setIsFocused(false)}
            onKeyDown={handleKeyDown}
            placeholder={isFull ? "Limite raggiunto" : placeholder}
            disabled={isFull}
            className={`w-full bg-transparent outline-none text-sm text-gray-900 placeholder-gray-400 ${isFull ? 'cursor-not-allowed' : ''}`}
          />
          
          {/* Dropdown in stile AuthPage */}
          {isFocused && !isFull && suggestions.length > 0 && (
            <ul className="absolute z-10 w-full mt-2 max-h-48 overflow-y-auto rounded-lg border border-gray-200 bg-white shadow-lg p-0 m-0 list-none">
              {suggestions.map(option => (
                <li 
                  key={option.label}
                  onMouseDown={(e) => {
                    e.preventDefault();
                    onAdd(isMacro ? option.macroTag : option.microTag);
                    setVal("");
                  }}
                  className="px-4 py-2 cursor-pointer text-sm text-gray-700 hover:bg-gray-100 border-b border-gray-50 last:border-0"
                >
                  {/* Aggiungi questo blocco per i Micro Tag */}
                  {!isMacro && (
                    <div className="text-[10px] text-gray-400 uppercase font-bold mb-0.5">
                      {option.macroTag}
                    </div>
                  )}
                  {isMacro ? option.macroTag : option.microTag}
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>
      {error && <p className="mt-1 text-sm font-medium text-red-600">{error}</p>}
    </div>
  );
}

function EditAreaButton({ icon, label, active, onClick }) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={`flex items-center justify-between rounded-lg border px-4 py-4 text-left font-semibold transition ${
        active
          ? "border-blue-600 bg-blue-50 text-blue-700"
          : "border-gray-200 text-gray-700 hover:bg-gray-50"
      }`}
    >
      <span className="flex items-center gap-3">
        {icon}
        {label}
      </span>
      <ChevronRight className="h-4 w-4" />
    </button>
  );
}

function Field({ id, label, value, onChange, error, type = "text" }) {
  return (
    <div>
      <label htmlFor={id} className="mb-2 block text-sm font-semibold text-gray-700">
        {label}
      </label>
      <input
        id={id}
        type={type}
        value={value}
        onChange={(event) => onChange(event.target.value)}
        className={`w-full rounded-lg border px-4 py-3 text-gray-900 outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-100 ${
          error ? "border-red-500" : "border-gray-300"
        }`}
      />
      {error && <p className="mt-2 text-sm font-medium text-red-600">{error}</p>}
    </div>
  );
}

function CardMessage({ message }) {
  if (!message) return null;

  const isError =
    message.includes("obbligatori") ||
    message.includes("non valida") ||
    message.includes("non corretta") ||
    message.includes("registrata") ||
    message.includes("troppo corta") ||
    message.includes("maiuscola") ||
    message.includes("numero") ||
    message.includes("errori"); 

  return (
    <p
      className={`mt-4 text-sm font-semibold ${
        isError ? "text-red-600" : "text-green-600"
      }`}
    >
      {message}
    </p>
  );
}