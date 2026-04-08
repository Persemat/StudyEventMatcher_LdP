import { User, Mail, LogOut } from "lucide-react";
import { useNavigate } from "react-router-dom";

export default function Profile({ user, onLogout }) {
  const navigate = useNavigate();

  const handleLogoutClick = async () => {
    await onLogout(); // Esegue la fetch e setLoggedUser(null)
    navigate("/login");
  };

  // Se l'utente non è loggato, non mostriamo nulla
  if (!user) {
    return null;
  }

  return (
    <div className="space-y-6">
      {/* Header con titolo e Logout */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Il Mio Profilo</h1>
          <p className="text-gray-600 mt-1">
            Visualizza le tue informazioni personali
          </p>
        </div>
        <button
          onClick={handleLogoutClick}
          className="bg-red-600 text-white px-6 py-3 rounded-lg font-medium hover:bg-red-700 transition-colors flex items-center gap-2"
        >
          <LogOut className="w-5 h-5" />
          Logout
        </button>
      </div>

      {/* Profile Card */}
      <div className="bg-white rounded-lg shadow overflow-hidden">
        {/* Header con Avatar e info rapide */}
        <div className="p-8 border-b border-gray-200 bg-gray-50/50">
          <div className="flex items-center gap-6">
            <div className="w-24 h-24 bg-blue-100 rounded-full flex items-center justify-center border-4 border-white shadow-sm">
              <User className="w-12 h-12 text-blue-600" />
            </div>
            <div>
              <h2 className="text-2xl font-bold text-gray-900">
                {user.nome || user.name}
              </h2>
              <p className="text-gray-600 font-medium">{user.email}</p>
            </div>
          </div>
        </div>

        {/* Informazioni Dettagliate */}
        <div className="p-8 space-y-8">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
            {/* Campo Nome */}
            <div className="flex items-start gap-4">
              <div className="p-3 bg-blue-50 rounded-lg">
                <User className="w-6 h-6 text-blue-600" />
              </div>
              <div>
                <p className="text-sm font-semibold text-gray-500 uppercase tracking-wider">
                  Nome Completo
                </p>
                <p className="text-lg font-medium text-gray-900 mt-1">
                  {user.nome || user.name}
                </p>
              </div>
            </div>

            {/* Campo Email */}
            <div className="flex items-start gap-4">
              <div className="p-3 bg-blue-50 rounded-lg">
                <Mail className="w-6 h-6 text-blue-600" />
              </div>
              <div>
                <p className="text-sm font-semibold text-gray-500 uppercase tracking-wider">
                  Indirizzo Email
                </p>
                <p className="text-lg font-medium text-gray-900 mt-1">
                  {user.email}
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}