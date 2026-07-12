import { ShieldX } from "lucide-react";
import { Link } from "react-router-dom";

function AccessDeniedPage() {
  return (
    <main className="flex min-h-screen items-center justify-center bg-slate-100 px-5">
      <div className="max-w-md text-center">
        <div className="mx-auto flex size-16 items-center justify-center rounded-2xl bg-red-100 text-red-600">
          <ShieldX size={32} />
        </div>

        <h1 className="mt-6 text-3xl font-bold text-slate-950">
          Access denied
        </h1>

        <p className="mt-3 text-slate-600">
          Your account does not have permission to open this page.
        </p>

        <Link
          to="/dashboard"
          className="mt-7 inline-flex rounded-xl bg-blue-600 px-5 py-3 text-sm font-semibold text-white hover:bg-blue-700"
        >
          Return to dashboard
        </Link>
      </div>
    </main>
  );
}

export default AccessDeniedPage;