// Authentication page.
import React, { useState, useEffect, useMemo, useRef } from 'react'
import { useNavigate, Link } from "react-router-dom";

import { loadCityOptions, loadTagOptions } from "./catalogs";

const MAX_MACRO_TAGS = 3;
const MAX_MICRO_TAGS = 5;

export default function AuthPage(props) {
  const [isLogin, setIsLogin] = useState(true);
  const navigate = useNavigate();

  // Shared form fields.
  const [name, setName] = useState('');
  const [lastname, setLastname] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  // Registration-only fields.
  const [city, setCity] = useState('');
  const [macroTags, setMacroTags] = useState([]);
  const [microTags, setMicroTags] = useState([]);

  // Catalog data.
  const [cityOptions, setCityOptions] = useState([]);
  const [tagOptions, setTagOptions] = useState([]);

  // UI state for focused autocomplete fields.
  const [isCityInputFocused, setIsCityInputFocused] = useState(false);
  
  const [macroInput, setMacroInput] = useState('');
  const [isMacroInputFocused, setIsMacroInputFocused] = useState(false);
  
  const [microInput, setMicroInput] = useState('');
  const [isMicroInputFocused, setIsMicroInputFocused] = useState(false);

  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  // Input refs used to restore focus.
  const cityInputRef = useRef(null);

  // Loads catalogs when the registration form is active.
  useEffect(() => {
    let active = true;
    if (!isLogin) {
      loadCityOptions()
        .then((data) => active && setCityOptions(data))
        .catch(() => active && setError("Impossibile caricare il catalogo comuni."));

      loadTagOptions()
        .then((data) => active && setTagOptions(data))
        .catch(() => active && setError("Impossibile caricare il catalogo tag."));
    }
    return () => { active = false; };
  }, [isLogin]);

  // City autocomplete suggestions.
  const citySuggestions = useMemo(() => {
    const query = city.trim().toLowerCase();
    if (!query) return [];
    return cityOptions
      .filter((option) => option.name.toLowerCase().startsWith(query))
      .slice(0, 30);
  }, [city, cityOptions]);

  const validCityLabels = useMemo(
    () => new Set(cityOptions.map((option) => option.label)),
    [cityOptions]
  );

  // Macro tag autocomplete suggestions.
  const macroSuggestions = useMemo(() => {
    const query = macroInput.trim().toLowerCase();
    return tagOptions
      .filter(option => option.isMacro) // Keep macro tags only.
      .filter(option => !macroTags.includes(option.macroTag)) // Exclude selected tags.
      .filter(option => !query || option.macroTag.toLowerCase().includes(query)); // Match typed text.
  }, [macroInput, tagOptions, macroTags]);

  // Micro tag autocomplete suggestions.
  const microSuggestions = useMemo(() => {
    const query = microInput.trim().toLowerCase();
    return tagOptions
      .filter(option => !option.isMacro) // Keep micro tags only.
      .filter(option => !microTags.includes(option.microTag)) // Exclude selected tags.
      .filter(option => !query || option.microTag.toLowerCase().includes(query)); // Match typed text.
  }, [microInput, tagOptions, microTags]);


  function resetMessages() {
    setMessage('');
    setError('');
  }

  // Tag add/remove helpers.
  const addMacro = (macroValue) => {
    if (macroTags.length >= MAX_MACRO_TAGS) return; // Safety limit.
    if (!macroTags.includes(macroValue)) {
      setMacroTags([...macroTags, macroValue]);
    }
    setMacroInput("");
    setIsMacroInputFocused(false);
  };

  const removeMacro = (macroToRemove) => {
    setMacroTags(macroTags.filter(t => t !== macroToRemove));
  };

  const addMicro = (microValue) => {
    if (microTags.length >= MAX_MICRO_TAGS) return; // Safety limit.
    if (!microTags.includes(microValue)) {
      setMicroTags([...microTags, microValue]);
    }
    setMicroInput("");
    setIsMicroInputFocused(false);
  };

  const removeMicro = (microToRemove) => {
    setMicroTags(microTags.filter(t => t !== microToRemove));
  };


  async function handleSubmit(event) {
    event.preventDefault();
    resetMessages();

    // Registration-only validation.
    if (!isLogin) {
      if (password !== confirmPassword) {
        setError('Le password non corrispondono');
        return;
      }

      if (!city.trim()) {
        setError("La città preferita è richiesta.");
        cityInputRef.current?.focus();
        return;
      }

      // Validates the city against the catalog to avoid invalid values.
      if (!validCityLabels.has(city.trim())) {
        setError("Seleziona una città valida dall'elenco suggerito.");
        cityInputRef.current?.focus();
        return;
      }

      if (macroTags.length === 0) {
        setError('Devi selezionare almeno un Macro Tag di interesse.');
        return;
      }
    }

    try {
      const response = await fetch(isLogin ? '/api/user/login' : '/api/user', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(
          isLogin
            ? { email, password }
            : { 
                name, 
                lastname, 
                email, 
                password,
                favouriteCity: city.trim(), // Send the selected city label to the backend.
                macroTags: macroTags,
                microTags: microTags 
              }
        ),
        credentials: 'include'
      });

      if (!response.ok) {
        const errorText = await response.text();
        if (isLogin && errorText.includes("Devi confermare la tua email")) {
          setError(
            <span>
              Devi confermare la tua email prima di accedere.{" "}
              <button
                type="button"
                onClick={() => navigate("/verify-email", { state: { email } })}
                style={{
                  background: 'none',
                  border: 'none',
                  color: '#2563eb',
                  textDecoration: 'underline',
                  fontWeight: 'bold',
                  cursor: 'pointer',
                  padding: 0,
                  fontSize: 'inherit'
                }}
              >
                Clicca qui per confermare la tua email
              </button>
            </span>
          );
          return;
        }
        throw new Error(errorText);
      }

      const data = await response.json();

      if (isLogin) {
        setMessage(`Loggato come: ${data.email}`);
        props.onLoginSuccess(data);

        setTimeout(() => {
          navigate("/dashboard");
        }, 1000);

      } else {
        setMessage(
          `Registrazione completata per: ${data.name ?? ''} ${data.lastname ?? ''}. Reindirizzamento alla verifica...`
        );

        // Pass the email through router state for the verification page.
        const registeredEmail = email;

        // Reset form fields.
        setName('');
        setLastname('');
        setEmail('');
        setPassword('');
        setConfirmPassword('');
        setCity('');
        setMacroTags([]);
        setMicroTags([]);

        setTimeout(() => {
          // Navigate to verification while keeping the email out of the URL.
          navigate("/verify-email", { state: { email: registeredEmail } });
        }, 1500);
      }

    } catch (error) {
      console.error(error);
      setError(error.message);
    }
  }

  return (
    <main style={styles.container}>
      <section style={styles.wrapper}>
        <div style={styles.logoCircle}>📖</div>

        <h1 style={styles.title}>StudyTogether</h1>
        <br />

        <p style={styles.subtitle}>
          {isLogin
            ? 'Bentornato! Entra nel tuo account'
            : 'Crea un tuo account e inizia a studiare con gli altri studenti'}
        </p>

        <div style={styles.card}>
          <div style={styles.tabs}>
            <button
              type="button"
              onClick={() => { setIsLogin(true); resetMessages(); }}
              style={{ ...styles.tabButton, ...(isLogin ? styles.activeTab : {}), }}
            >
              Accedi
            </button>

            <button
              type="button"
              onClick={() => { setIsLogin(false); resetMessages(); }}
              style={{ ...styles.tabButton, ...(!isLogin ? styles.activeTab : {}), }}
            >
              Registrati
            </button>
          </div>

          <form onSubmit={handleSubmit} style={styles.form}>

            {!isLogin && (
              <>
                <div style={styles.row}>
                  <div style={{ ...styles.field, flex: 1 }}>
                    <label htmlFor="name" style={styles.label}>Nome</label>
                    <input
                      id="name"
                      type="text"
                      value={name}
                      onChange={(e) => setName(e.target.value)}
                      placeholder="Mario"
                      required
                      style={styles.input}
                    />
                  </div>

                  <div style={{ ...styles.field, flex: 1 }}>
                    <label htmlFor="lastname" style={styles.label}>Cognome</label>
                    <input
                      id="lastname"
                      type="text"
                      value={lastname}
                      onChange={(e) => setLastname(e.target.value)}
                      placeholder="Rossi"
                      required
                      style={styles.input}
                    />
                  </div>
                </div>

                {/* City section */}
                <div style={styles.field} className="relative">
                  <label className="block text-sm font-medium text-gray-700 mb-1" style={styles.label}>
                    Città preferita
                  </label>
                  <input
                    ref={cityInputRef}
                    type="text"
                    required
                    placeholder="es: Milano"
                    style={styles.input}
                    value={city}
                    onChange={(e) => setCity(e.target.value)}
                    onFocus={() => setIsCityInputFocused(true)}
                    onBlur={() => setIsCityInputFocused(false)}
                  />
                  {isCityInputFocused && citySuggestions.length > 0 && (
                    <ul style={styles.dropdown}>
                      {citySuggestions.map((option) => (
                        <li
                          key={option.label}
                          onMouseDown={(e) => {
                            e.preventDefault();
                            setCity(option.label);
                            setIsCityInputFocused(false);
                          }}
                          style={styles.dropdownItem}
                        >
                          {option.label}
                        </li>
                      ))}
                    </ul>
                  )}
                </div>

                {/* Macro tags section */}
                <div style={styles.field}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <label style={styles.label}>Inserisci almeno un'area di interesse</label>
                    <span style={{ 
                      fontSize: '12px', 
                      fontWeight: 'bold', 
                      color: macroTags.length >= MAX_MACRO_TAGS ? '#dc2626' : '#64748b' 
                    }}>
                      {macroTags.length} / {MAX_MACRO_TAGS}
                    </span>
                  </div>
                  
                  <div style={{ 
                    ...styles.tagsContainer, 
                    backgroundColor: macroTags.length >= MAX_MACRO_TAGS ? '#e2e8f0' : '#f1f5f9' 
                  }}>
                    {macroTags.map(m => (
                      <span key={m} style={styles.tagPill}>
                        {m} 
                        <button type="button" onClick={() => removeMacro(m)} style={styles.tagRemove}>×</button>
                      </span>
                    ))}
                    <div style={{ position: 'relative', flex: 1 }}>
                      <input
                        type="text"
                        value={macroInput}
                        onChange={(e) => {
                          setMacroInput(e.target.value);
                          setIsMacroInputFocused(true);
                        }}
                        onFocus={() => setIsMacroInputFocused(true)}
                        onBlur={() => setIsMacroInputFocused(false)}
                        placeholder={macroTags.length >= MAX_MACRO_TAGS ? "Limite raggiunto" : "Cerca area di interesse..."}
                        disabled={macroTags.length >= MAX_MACRO_TAGS}
                        style={{
                          ...styles.tagInput, 
                          cursor: macroTags.length >= MAX_MACRO_TAGS ? 'not-allowed' : 'text'
                        }}
                      />
                      {isMacroInputFocused && macroTags.length < MAX_MACRO_TAGS && macroSuggestions.length > 0 && (
                        <ul style={styles.dropdown}>
                          {macroSuggestions.map(option => (
                            <li 
                              key={option.macroTag} 
                              onMouseDown={(e) => {
                                e.preventDefault();
                                addMacro(option.macroTag);
                              }} 
                              style={styles.dropdownItem}
                            >
                              {option.macroTag}
                            </li>
                          ))}
                        </ul>
                      )}
                    </div>
                  </div>
                </div>

                {/* Micro tags section */}
                <div style={styles.field}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <label style={styles.label}>Scegli fino a 5 argomenti di interesse specifico</label>
                    <span style={{ 
                      fontSize: '12px', 
                      fontWeight: 'bold', 
                      color: microTags.length >= MAX_MICRO_TAGS ? '#dc2626' : '#64748b' 
                    }}>
                      {microTags.length} / {MAX_MICRO_TAGS}
                    </span>
                  </div>

                  <div style={{ 
                    ...styles.tagsContainer, 
                    backgroundColor: microTags.length >= MAX_MICRO_TAGS ? '#e2e8f0' : '#f1f5f9' 
                  }}>
                    {microTags.map(m => (
                      <span key={m} style={styles.tagPillMicro}>
                        {m} 
                        <button type="button" onClick={() => removeMicro(m)} style={styles.tagRemove}>×</button>
                      </span>
                    ))}
                    <div style={{ position: 'relative', flex: 1 }}>
                      <input
                        type="text"
                        value={microInput}
                        onChange={(e) => {
                          setMicroInput(e.target.value);
                          setIsMicroInputFocused(true);
                        }}
                        onFocus={() => setIsMicroInputFocused(true)}
                        onBlur={() => setIsMicroInputFocused(false)}
                        placeholder={microTags.length >= MAX_MICRO_TAGS ? "Limite raggiunto" : "Cerca competenza..."}
                        disabled={microTags.length >= MAX_MICRO_TAGS}
                        style={{
                          ...styles.tagInput, 
                          cursor: microTags.length >= MAX_MICRO_TAGS ? 'not-allowed' : 'text'
                        }}
                      />
                      {isMicroInputFocused && microTags.length < MAX_MICRO_TAGS && microSuggestions.length > 0 && (
                        <ul style={styles.dropdown}>
                          {microSuggestions.map(option => (
                            <li 
                              key={option.microTag} 
                              onMouseDown={(e) => {
                                e.preventDefault();
                                addMicro(option.microTag);
                              }} 
                              style={styles.dropdownItem}
                            >
                              <div style={{ fontSize: '11px', color: '#64748b', textTransform: 'uppercase' }}>
                                {option.macroTag}
                              </div>
                              {option.microTag}
                            </li>
                          ))}
                        </ul>
                      )}
                    </div>
                  </div>
                </div>
              </>
            )}

            <div style={styles.field}>
              <label htmlFor="email" style={styles.label}>Email</label>
              <input
                id="email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="mario.rossi@example.com"
                required
                style={styles.input}
              />
            </div>

            <div style={styles.field}>
              <label htmlFor="password" style={styles.label}>Password</label>
              <div style={styles.passwordWrapper}>
                <input
                  id="password"
                  type={showPassword ? 'text' : 'password'}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  required
                  style={styles.passwordInput}
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  style={styles.eyeButton}
                >
                  👁
                </button>
              </div>
            </div>

            {!isLogin && (
              <div style={styles.field}>
                <label htmlFor="confirmPassword" style={styles.label}>Conferma password</label>

                <div style={styles.passwordWrapper}>
                  <input
                    id="confirmPassword"
                    type={showConfirmPassword ? 'text' : 'password'}
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    placeholder="••••••••"
                    required
                    style={styles.passwordInput}
                  />
                  <button
                    type="button"
                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                    style={styles.eyeButton}
                  >
                    👁
                  </button>
                </div>
              </div>
            )}

            {isLogin && (
              <Link to="/forgot-password" style={styles.linkButton}>
                Password dimenticata?
              </Link>
            )}

            <button type="submit" style={styles.submitButton}>
              {isLogin ? 'Accedi' : 'Registrati'}
            </button>

          </form>

          {message && <p style={styles.success}>{message}</p>}
          {error && <p style={styles.error}>{error}</p>}
        </div>
      </section>
    </main>
  )
}

const styles = {
  container: {
    minHeight: '100vh',
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#eef3ff',
    fontFamily: 'Arial, sans-serif',
    padding: '24px',
  },
  wrapper: {
    width: '100%',
    maxWidth: '620px',
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
  },
  logoCircle: {
    width: '72px',
    height: '72px',
    borderRadius: '50%',
    backgroundColor: '#2563eb',
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    fontSize: '30px',
    marginBottom: '20px',
  },
  title: {
    fontSize: '56px',
    fontWeight: '800',
    color: '#0f172a',
    marginBottom: '10px',
    textAlign: 'center',
  },
  subtitle: {
    fontSize: '18px',
    color: '#334155',
    marginBottom: '32px',
    textAlign: 'center',
  },
  card: {
    width: '100%',
    backgroundColor: '#ffffff',
    padding: '28px',
    borderRadius: '22px',
    boxShadow: '0 10px 30px rgba(0,0,0,0.08)',
  },
  tabs: {
    display: 'grid',
    gridTemplateColumns: '1fr 1fr',
    gap: '8px',
    backgroundColor: '#f1f5f9',
    borderRadius: '14px',
    padding: '6px',
    marginBottom: '28px',
  },
  tabButton: {
    padding: '16px',
    border: 'none',
    borderRadius: '12px',
    backgroundColor: 'transparent',
    fontSize: '18px',
    fontWeight: '600',
    cursor: 'pointer',
    color: '#334155',
  },
  activeTab: {
    backgroundColor: '#ffffff',
    color: '#2563eb',
    boxShadow: '0 2px 8px rgba(0,0,0,0.08)',
  },
  form: {
    display: 'flex',
    flexDirection: 'column',
    gap: '18px',
  },
  row: {
    display: 'flex',
    gap: '12px',
    flexWrap: 'wrap',
  },
  field: {
    display: 'flex',
    flexDirection: 'column',
    gap: '8px',
    position: 'relative', // Anchors absolute dropdowns.
  },
  label: {
    fontWeight: '700',
    fontSize: '16px',
    color: '#1e293b',
  },
  input: {
    padding: '16px',
    border: '1px solid #cbd5e1',
    borderRadius: '14px',
    fontSize: '16px',
    outline: 'none',
    backgroundColor: '#f1f5f9',
    color: '#000000',
    width: '100%',
    boxSizing: 'border-box'
  },
  passwordWrapper: {
    display: 'flex',
    alignItems: 'center',
    border: '1px solid #cbd5e1',
    borderRadius: '14px',
    backgroundColor: '#f1f5f9',
  },
  passwordInput: {
    flex: 1,
    padding: '16px',
    border: 'none',
    fontSize: '16px',
    outline: 'none',
    borderRadius: '14px 0 0 14px',
    backgroundColor: 'transparent',
    color: '#000000',
  },
  eyeButton: {
    border: 'none',
    backgroundColor: 'transparent',
    padding: '0 16px',
    cursor: 'pointer',
    fontSize: '20px',
    color: '#64748b',
  },
  submitButton: {
    marginTop: '8px',
    padding: '16px',
    border: 'none',
    borderRadius: '14px',
    cursor: 'pointer',
    fontSize: '18px',
    fontWeight: '700',
    backgroundColor: '#2563eb',
    color: '#ffffff',
  },
  success: {
    marginTop: '18px',
    color: '#16a34a',
    fontWeight: '600',
    textAlign: 'center',
  },
  error: {
    marginTop: '18px',
    color: '#dc2626',
    fontWeight: '600',
    textAlign: 'center',
  },
  linkButton: {
    color: '#667eea',
    textDecoration: 'none',
    fontWeight: '600',
    textAlign: 'left',
    display: 'block',
    cursor: 'pointer',
  },
  // Tag and dropdown styles.
  tagsContainer: {
    display: 'flex',
    flexWrap: 'wrap',
    gap: '8px',
    padding: '10px',
    border: '1px solid #cbd5e1',
    borderRadius: '14px',
    backgroundColor: '#f1f5f9',
    alignItems: 'center',
    minHeight: '52px',
    boxSizing: 'border-box',
    transition: 'background-color 0.2s ease'
  },
  tagPill: {
    backgroundColor: '#2563eb', // Macro tag color.
    color: 'white',
    padding: '4px 12px',
    borderRadius: '20px',
    fontSize: '14px',
    display: 'flex',
    alignItems: 'center',
    gap: '6px'
  },
  tagPillMicro: {
    backgroundColor: '#10b981', // Micro tag color.
    color: 'white',
    padding: '4px 12px',
    borderRadius: '20px',
    fontSize: '14px',
    display: 'flex',
    alignItems: 'center',
    gap: '6px'
  },
  tagRemove: {
    background: 'transparent',
    border: 'none',
    color: 'white',
    cursor: 'pointer',
    fontSize: '16px',
    padding: 0,
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    lineHeight: '1'
  },
  tagInput: {
    border: 'none',
    background: 'transparent',
    outline: 'none',
    fontSize: '16px',
    padding: '4px',
    width: '100%',
    minWidth: '150px'
  },
  dropdown: {
    position: 'absolute',
    top: '100%',
    left: 0,
    right: 0,
    backgroundColor: 'white',
    border: '1px solid #cbd5e1',
    borderRadius: '10px',
    marginTop: '4px',
    maxHeight: '200px',
    overflowY: 'auto',
    listStyle: 'none',
    padding: '0',
    zIndex: 10,
    boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
    margin: 0
  },
  dropdownItem: {
    padding: '10px 14px',
    cursor: 'pointer',
    borderBottom: '1px solid #f1f5f9',
    fontSize: '14px',
    color: '#334155',
    backgroundColor: 'white'
  }
}
