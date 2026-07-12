import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom';
import { AlertTriangle, Bus, CheckCircle2, Eye, EyeOff, ShieldCheck } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { loginSchema } from '../schemas';

const PLATFORM_ROLES = ['Admin', 'Fleet Manager', 'Driver', 'Safety Officer', 'Financial Analyst'];

export default function LoginPage() {
  const { login, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [showPassword, setShowPassword] = useState(false);
  const [apiError, setApiError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
    setValue,
  } = useForm({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: '', password: '', remember: false },
  });

  useEffect(() => {
    if (location.state?.email) setValue('email', location.state.email);
  }, [location.state?.email, setValue]);

  if (isAuthenticated) return <Navigate to="/dashboard" replace />;

  const onSubmit = async ({ email, password }) => {
    setApiError('');
    setSubmitting(true);
    try {
      await login({ email, password });
      navigate('/dashboard', { replace: true });
    } catch (error) {
      setApiError(error?.message || 'Invalid email or password');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen flex bg-[#0d0d0d]">
      <div className="hidden lg:flex flex-col justify-between w-[520px] bg-[#0a0a0a] border-r border-[#1a1a1a] p-12">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-[#d97706] flex items-center justify-center">
            <Bus className="h-6 w-6 text-white" />
          </div>
          <div>
            <p className="text-base font-bold text-[#f0f0f0]">TransitOps</p>
            <p className="text-xs text-[#6b7280]">Smart Transport Operations</p>
          </div>
        </div>

        <div>
          <h1 className="text-4xl font-bold text-[#f0f0f0] leading-tight mb-4">
            One platform.
            <br />Five secure roles.
          </h1>
          <p className="text-[#6b7280] text-sm mb-8">
            Sign in with your assigned role. The dashboard and navigation automatically adapt to your backend permissions.
          </p>
          <div className="grid grid-cols-1 gap-3">
            {PLATFORM_ROLES.map((role) => (
              <div key={role} className="p-3 rounded-xl bg-[#141414] border border-[#2a2a2a] flex items-center gap-3">
                <ShieldCheck className="h-4 w-4 text-[#d97706]" />
                <span className="text-sm text-[#d1d5db]">{role}</span>
              </div>
            ))}
          </div>
        </div>

        <p className="text-[10px] text-[#4b5563]">© 2026 TransitOps. All rights reserved.</p>
      </div>

      <div className="flex-1 flex items-center justify-center p-8">
        <div className="w-full max-w-md">
          <div className="flex items-center gap-3 mb-8 lg:hidden">
            <div className="w-9 h-9 rounded-lg bg-[#d97706] flex items-center justify-center">
              <Bus className="h-5 w-5 text-white" />
            </div>
            <p className="text-lg font-bold text-[#f0f0f0]">TransitOps</p>
          </div>

          <div className="mb-8">
            <h2 className="text-2xl font-bold text-[#f0f0f0]">Sign in</h2>
            <p className="text-sm text-[#6b7280] mt-1">Enter your credentials to access your role-based dashboard.</p>
          </div>

          {location.state?.registrationSuccess && (
            <div className="flex items-start gap-3 p-4 bg-emerald-900/20 border border-emerald-900/40 rounded-xl mb-6">
              <CheckCircle2 className="h-4 w-4 text-emerald-400 mt-0.5 shrink-0" />
              <p className="text-sm text-emerald-400">Driver account created successfully. You can sign in now.</p>
            </div>
          )}

          {apiError && (
            <div className="flex items-start gap-3 p-4 bg-red-900/20 border border-red-900/40 rounded-xl mb-6">
              <AlertTriangle className="h-4 w-4 text-red-400 mt-0.5 shrink-0" />
              <p className="text-sm text-red-400">{apiError}</p>
            </div>
          )}

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
            <div>
              <label className="label-dark">Email Address</label>
              <input {...register('email')} type="email" className="input-dark" placeholder="you@transitops.com" autoComplete="email" />
              {errors.email && <p className="text-xs text-red-400 mt-1">{errors.email.message}</p>}
            </div>

            <div>
              <label className="label-dark">Password</label>
              <div className="relative">
                <input
                  {...register('password')}
                  type={showPassword ? 'text' : 'password'}
                  className="input-dark pr-10"
                  placeholder="••••••••"
                  autoComplete="current-password"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword((value) => !value)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-[#6b7280] hover:text-[#f0f0f0]"
                  aria-label={showPassword ? 'Hide password' : 'Show password'}
                >
                  {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                </button>
              </div>
              {errors.password && <p className="text-xs text-red-400 mt-1">{errors.password.message}</p>}
            </div>

            <label className="flex items-center gap-2 cursor-pointer">
              <input {...register('remember')} type="checkbox" className="w-4 h-4 rounded border-[#2a2a2a] bg-[#0d0d0d] accent-[#d97706]" />
              <span className="text-xs text-[#9ca3af]">Remember me on this device</span>
            </label>

            <button type="submit" disabled={submitting} className="btn-amber w-full justify-center py-3 text-base">
              {submitting ? 'Signing in…' : 'Sign In'}
            </button>
          </form>

          <div className="mt-6 text-center">
            <p className="text-sm text-[#9ca3af]">
              Need a Driver account?{' '}
              <Link to="/signup" className="text-[#d97706] hover:text-[#f59e0b]">Create one</Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
