import { MapPinOff } from "lucide-react";
import { Link } from "react-router-dom";

function NotFoundPage() {
  return (
    <main className="flex min-h-screen items-center justify-center bg-slate-100 px-5">
      <div className="max-w-md text-center">
        <div className="mx-auto flex size-16 items-center justify-center rounded-2xl bg-blue-100 text-blue-600">
          <MapPinOff size={31} />
        </div>

        <p className="mt-6 text-sm font-bold uppercase tracking-widest text-blue-600">
          Error 404
        </p>

        <h1 className="mt-2 text-3xl font-bold text-slate-950">
          Page not found
        </h1>

        <p className="mt-3 text-slate-600">
          The page you requested does not exist or has been moved.
        </p>

        <Link
          to="/dashboard"
          className="mt-7 inline-flex rounded-xl bg-blue-600 px-5 py-3 text-sm font-semibold text-white hover:bg-blue-700"
        >
          Open dashboard
        </Link>
      </div>
    </main>
  );
}

export default NotFoundPage;