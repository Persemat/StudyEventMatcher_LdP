//Profile
import { useEffect, useState } from "react";
import { Bell, ChevronRight, Lock, LogOut, Mail, Save, Settings, User} from "lucide-react";
import { useNavigate } from "react-router-dom";

export default function Profile({ user, onLogout, onUserUpdated }) {
  const navigate = useNavigate();

  const [personalForm, setPersonalForm] = useState({ name: "", lastname: "" });
  const [emailForm, setEmailForm] = useState({ email: "" });
  const [passwordForm, setPasswordForm] = useState({
    currentPassword: "",
    newPassword: "",
    confirmPassword: "",
  });

  const [personalErrors, setPersonalErrors] = useState({});
  const [emailErrors, setEmailErrors] = useState({});
  const [passwordErrors, setPasswordErrors] = useState({});
  const [messages, setMessages] = useState({});
  const [loading, setLoading] = useState("");
  const [activeSection, setActiveSection] = useState("");
  const [activeEditArea, setActiveEditArea] = useState("");

  useEffect(() => {
    if (!user) return;

    setPersonalForm({
      name: user.name ?? "",
      lastname: user.lastname ?? "",
    });
    setEmailForm({ email: user.email ?? "" });
  }, [user]);

  const handleLogoutClick = async () => {
    await onLogout();
    navigate("/login");
  };

  const resetCardState = (card) => {
    setMessages((prev) => ({ ...prev, [card]: "" }));

    if (card === "personal") setPersonalErrors({});
    if (card === "email") setEmailErrors({});
    if (card === "password") setPasswordErrors({});
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
        email: "Dati personali aggiornati con successo",
      }));
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
    } catch (error) {
      if (error.message === "Password attuale non corretta") {
        setPasswordErrors({ currentPassword: error.message });
      } else if (error.message === "Password attuale obbligatoria") {
        setPasswordErrors({ currentPassword: error.message });
      } else if (error.message === "Conferma password obbligatoria") {
        setPasswordErrors({ confirmPassword: error.message });
      } else if (error.message === "Le password non corrispondono") {
        setPasswordErrors({ confirmPassword: error.message });
      } else {
        setPasswordErrors({ newPassword: error.message });
      }
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
            <button
              type="button"
              disabled
              className="flex cursor-not-allowed items-center justify-center gap-2 rounded-lg border border-gray-200 px-4 py-3 text-sm font-semibold text-gray-400"
            >
              <Settings className="w-4 h-4" />
              Preferenze
              <span className="text-xs font-medium">(Non disponibile)</span>
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
              disabled
              className="flex cursor-not-allowed items-center justify-center gap-2 rounded-lg border border-gray-200 px-4 py-3 text-sm font-semibold text-gray-400"
            >
              <Bell className="w-4 h-4" />
              Impostazioni notifiche
              <span className="text-xs font-medium">(Non disponibile)</span>
            </button>
          </div>
        </div>

        <div className="p-6 sm:p-8">
          {activeSection === "data" && (
            <div className="space-y-6">
              <div className="grid grid-cols-1 gap-3 md:grid-cols-3">
                <EditAreaButton
                  icon={<User className="h-5 w-5" />}
                  label="Nome e cognome"
                  active={activeEditArea === "personal"}
                  onClick={() =>
                    setActiveEditArea((current) =>
                      current === "personal" ? "" : "personal"
                    )
                  }
                />
                <EditAreaButton
                  icon={<Mail className="h-5 w-5" />}
                  label="Email"
                  active={activeEditArea === "email"}
                  onClick={() =>
                    setActiveEditArea((current) =>
                      current === "email" ? "" : "email"
                    )
                  }
                />
                <EditAreaButton
                  icon={<Lock className="h-5 w-5" />}
                  label="Password"
                  active={activeEditArea === "password"}
                  onClick={() =>
                    setActiveEditArea((current) =>
                      current === "password" ? "" : "password"
                    )
                  }
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
                <Field
                  id="name"
                  label="Nome"
                  value={personalForm.name}
                  error={personalErrors.name}
                  onChange={(value) =>
                    setPersonalForm((prev) => ({ ...prev, name: value }))
                  }
                />

                <Field
                  id="lastname"
                  label="Cognome"
                  value={personalForm.lastname}
                  error={personalErrors.lastname}
                  onChange={(value) =>
                    setPersonalForm((prev) => ({ ...prev, lastname: value }))
                  }
                />
              </div>

              <CardMessage message={messages.personal} />

              <button
                type="submit"
                disabled={loading === "personal"}
                className="mt-5 flex w-full items-center justify-center gap-2 rounded-lg bg-blue-600 px-4 py-3 font-semibold text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-70"
              >
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

              <Field
                id="email"
                type="email"
                label="Email"
                value={emailForm.email}
                error={emailErrors.email}
                onChange={(value) => setEmailForm({ email: value })}
              />

              <CardMessage message={messages.email} />

              <button
                type="submit"
                disabled={loading === "email"}
                className="mt-5 flex w-full items-center justify-center gap-2 rounded-lg bg-blue-600 px-4 py-3 font-semibold text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-70"
              >
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
                <Field
                  id="currentPassword"
                  type="password"
                  label="Password attuale"
                  value={passwordForm.currentPassword}
                  error={passwordErrors.currentPassword}
                  onChange={(value) =>
                    setPasswordForm((prev) => ({
                      ...prev,
                      currentPassword: value,
                    }))
                  }
                />

                <Field
                  id="newPassword"
                  type="password"
                  label="Nuova password"
                  value={passwordForm.newPassword}
                  error={passwordErrors.newPassword}
                  onChange={(value) =>
                    setPasswordForm((prev) => ({ ...prev, newPassword: value }))
                  }
                />

                <Field
                  id="confirmPassword"
                  type="password"
                  label="Conferma nuova password"
                  value={passwordForm.confirmPassword}
                  error={passwordErrors.confirmPassword}
                  onChange={(value) =>
                    setPasswordForm((prev) => ({
                      ...prev,
                      confirmPassword: value,
                    }))
                  }
                />
              </div>

              <CardMessage message={messages.password} />

              <button
                type="submit"
                disabled={loading === "password"}
                className="mt-5 flex w-full items-center justify-center gap-2 rounded-lg bg-blue-600 px-4 py-3 font-semibold text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-70"
              >
                <Save className="h-4 w-4" />
                {loading === "password" ? "Salvataggio..." : "Aggiorna password"}
              </button>
                </form>
              )}
            </div>
          )}
        </div>
      </div>
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
    message.includes("numero");

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
