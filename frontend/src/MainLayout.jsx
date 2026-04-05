import { Outlet, Link, useLocation } from "react-router-dom"; 
import { BookOpen, Calendar, User, Home } from "lucide-react";

export default function MainLayout() { 
  const location = useLocation();

  // Rimosso ": string" perché siamo in JS
  const isActive = (path) => {
    if (path === "/") {
      return location.pathname === "/";
    }
    return location.pathname.startsWith(path);
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white border-b border-gray-200 sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center gap-2">
              <BookOpen className="w-8 h-8 text-blue-600" />
              <span className="font-bold text-xl text-gray-900">StudyTogether</span>
            </div>
            <nav className="flex items-center gap-6">
                <Link
                to="/"
                className={`flex items-center gap-2 px-3 py-2 rounded-lg transition-colors ${
                  isActive("/") && location.pathname === "/"
                    ? "bg-blue-100 text-blue-700"
                    : "text-gray-600 hover:bg-gray-100"
                }`}
              >
                <Home className="w-5 h-5" />
                <span>Dashboard</span>
              </Link>

                <Link
                to="/eventi"
                className={`flex items-center gap-2 px-3 py-2 rounded-lg transition-colors ${
                  isActive("/eventi")
                    ? "bg-blue-100 text-blue-700"
                    : "text-gray-600 hover:bg-gray-100"
                }`}
              >
                <Calendar className="w-5 h-5" />
                <span>Tutti gli Eventi</span>
              </Link>

              <Link
                to="/miei-eventi"
                className={`flex items-center gap-2 px-3 py-2 rounded-lg transition-colors ${
                  isActive("/miei-eventi")
                    ? "bg-blue-100 text-blue-700"
                    : "text-gray-600 hover:bg-gray-100"
                }`}
              >
                <BookOpen className="w-5 h-5" />
                <span>I Miei Eventi</span>
              </Link>

              <Link
                to="/profilo"
                className={`flex items-center gap-2 px-3 py-2 rounded-lg transition-colors ${
                  isActive("/profilo")
                    ? "bg-blue-100 text-blue-700"
                    : "text-gray-600 hover:bg-gray-100"
                }`}
              >
                <User className="w-5 h-5" />
                <span>Profilo</span>
              </Link>
                
            </nav>
          </div>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 py-8">
        <Outlet /> {/* Qui appariranno le tue pagine */}
      </main>
    </div>
  );
}