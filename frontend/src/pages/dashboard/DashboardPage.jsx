import {
  BusFront,
  CircleDollarSign,
  LogOut,
  Route,
  UsersRound,
  Wrench,
} from "lucide-react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";

const cards = [
  {
    title: "Active Vehicles",
    value: "53",
    icon: BusFront,
  },
  {
    title: "Active Trips",
    value: "18",
    icon: Route,
  },
  {
    title: "Drivers On Duty",
    value: "26",
    icon: UsersRound,
  },
  {
    title: "In Maintenance",
    value: "5",
    icon: Wrench,
  },
];

function DashboardPage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login", { replace: true });
  };

  return (
    <main className="min-h-screen bg-slate-100">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-5 py-4">
          <div className="flex items-center gap-3">
            <div className="flex size-10 items-center justify-center rounded-xl bg-blue-600 text-white">
              <BusFront size={21} />
            </div>

            <div>
              <h1 className="font-bold text-slate-950">
                TransitOps
              </h1>
              <p className="text-xs text-slate-500">
                Operations Dashboard
              </p>
            </div>
          </div>

          <div className="flex items-center gap-4">
            <div className="hidden text-right sm:block">
              <p className="text-sm font-semibold text-slate-800">
                {user?.name}
              </p>
              <p className="text-xs text-slate-500">
                {user?.role}
              </p>
            </div>

            <button
              type="button"
              onClick={handleLogout}
              className="flex items-center gap-2 rounded-xl border border-slate-300 bg-white px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50"
            >
              <LogOut size={17} />
              Logout
            </button>
          </div>
        </div>
      </header>

      <section className="mx-auto max-w-7xl px-5 py-8">
        <div>
          <h2 className="text-2xl font-bold text-slate-950">
            Dashboard
          </h2>
          <p className="mt-1 text-sm text-slate-500">
            Welcome back, {user?.name}.
          </p>
        </div>

        <div className="mt-7 grid gap-5 sm:grid-cols-2 xl:grid-cols-4">
          {cards.map(({ title, value, icon: Icon }) => (
            <article
              key={title}
              className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm"
            >
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-slate-500">{title}</p>
                  <p className="mt-2 text-3xl font-bold text-slate-950">
                    {value}
                  </p>
                </div>

                <div className="flex size-12 items-center justify-center rounded-xl bg-blue-50 text-blue-600">
                  <Icon size={23} />
                </div>
              </div>
            </article>
          ))}
        </div>

        <div className="mt-6 grid gap-5 lg:grid-cols-2">
          <article className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
            <div className="flex items-center gap-3">
              <CircleDollarSign className="text-green-600" />
              <h3 className="font-semibold text-slate-900">
                Total Operational Cost
              </h3>
            </div>

            <p className="mt-5 text-4xl font-bold text-slate-950">
              ₹34,050
            </p>

            <p className="mt-2 text-sm text-slate-500">
              Temporary dashboard data. Detailed charts are added next.
            </p>
          </article>

          <article className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
            <h3 className="font-semibold text-slate-900">
              Authentication status
            </h3>

            <div className="mt-5 space-y-3 text-sm">
              <p>
                <span className="text-slate-500">Email:</span>{" "}
                <span className="font-medium text-slate-800">
                  {user?.email}
                </span>
              </p>

              <p>
                <span className="text-slate-500">Role:</span>{" "}
                <span className="font-medium text-slate-800">
                  {user?.role}
                </span>
              </p>

              <p className="font-medium text-green-700">
                JWT session is active.
              </p>
            </div>
          </article>
        </div>
      </section>
    </main>
  );
}

export default DashboardPage;