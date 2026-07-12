import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Link, Navigate, useNavigate } from 'react-router-dom';
import { AlertTriangle, Bus, Eye, EyeOff, ShieldCheck } from 'lucide-react';
import { signupSchema } from '../schemas';
import { authService } from '../services/authService';
import { useAuth } from '../context/AuthContext';

export default function SignUpPage() {
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();
  const [showPassword, setShowPassword] = useState(false);
  const [apiError, setApiError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(signupSchema),
    defaultValues: { name: '', email: '', password: '', confirmPassword: '' },
  });

  if (isAuthenticated) return <Navigate to="/dashboard" replace />;

  const onSubmit = async (data) => {
    setApiError('');
    setSubmitting(true);
    try {
      await authService.signup({
        name: data.name,
        email: data.email,
        password: data.password,
      });
      navigate('/login', {
        replace: true,
        state: { email: data.email, registrationSuccess: true },
      });
    } catch (error) {
      setApiError(error?.message || 'Unable to create your account');
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
            Join as a driver.
            <br />Start operating.
          </h1>
          <p className="text-[#6b7280] text-sm mb-8">
            Public registration creates a secure Driver account. Administrative roles can only be assigned by an Admin.
          </p>
          <div className="p-4 rounded-xl bg-[#141414] border border-[#2a2a2a] flex items-start gap-3">
            <ShieldCheck className="h-5 w-5 text-[#d97706] mt-0.5" />
            <div>
              <p className="text-sm font-semibold text-[#d1d5db]">Role: Driver</p>
              <p className="text-xs text-[#6b7280] mt-1">Access the dashboard, vehicles, drivers, and trip operations after signing in.</p>
            </div>
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
            <h2 className="text-2xl font-bold text-[#f0f0f0]">Create Driver Account</h2>
            <p className="text-sm text-[#6b7280] mt-1">Your account is activated immediately with the Driver role.</p>
          </div>

          {apiError && (
            <div className="flex items-start gap-3 p-4 bg-red-900/20 border border-red-900/40 rounded-xl mb-6">
              <AlertTriangle className="h-4 w-4 text-red-400 mt-0.5 shrink-0" />
              <p className="text-sm text-red-400">{apiError}</p>
            </div>
          )}

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
            <div>
              <label className="label-dark">Full Name</label>
              <input {...register('name')} className="input-dark" placeholder="Your name" autoComplete="name" />
              {errors.name && <p className="text-xs text-red-400 mt-1">{errors.name.message}</p>}
            </div>

            <div>
              <label className="label-dark">Email Address</label>
              <input {...register('email')} type="email" className="input-dark" placeholder="driver@example.com" autoComplete="email" />
              {errors.email && <p className="text-xs text-red-400 mt-1">{errors.email.message}</p>}
            </div>

            <div>
              <label className="label-dark">Assigned Role</label>
              <div className="input-dark flex items-center gap-2 text-[#d1d5db] bg-[#141414] cursor-not-allowed">
                <ShieldCheck className="h-4 w-4 text-[#d97706]" />
                DRIVER
              </div>
            </div>

            <div>
              <label className="label-dark">Password</label>
              <div className="relative">
                <input
                  {...register('password')}
                  type={showPassword ? 'text' : 'password'}
                  className="input-dark pr-10"
                  placeholder="Minimum 8 characters"
                  autoComplete="new-password"
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

            <div>
              <label className="label-dark">Confirm Password</label>
              <input
                {...register('confirmPassword')}
                type={showPassword ? 'text' : 'password'}
                className="input-dark"
                placeholder="Repeat your password"
                autoComplete="new-password"
              />
              {errors.confirmPassword && <p className="text-xs text-red-400 mt-1">{errors.confirmPassword.message}</p>}
            </div>

            <button type="submit" disabled={submitting} className="btn-amber w-full justify-center py-3 text-base">
              {submitting ? 'Creating account…' : 'Create Driver Account'}
            </button>
          </form>

          <div className="mt-6 text-center">
            <p className="text-sm text-[#9ca3af]">
              Already have an account?{' '}
              <Link to="/login" className="text-[#d97706] hover:text-[#f59e0b]">Sign in</Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
