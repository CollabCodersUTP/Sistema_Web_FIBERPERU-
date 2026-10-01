import React from 'react';

export default function Home() {
  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col font-sans">
      {/* Header / Navbar */}
      <header className="border-b border-slate-800 bg-slate-900/80 backdrop-blur sticky top-0 z-50 px-6 py-4 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-cyan-500 to-blue-600 flex items-center justify-between p-2 shadow-lg shadow-cyan-500/20">
            <svg className="w-full h-full text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
            </svg>
          </div>
          <div>
            <h1 className="font-bold text-lg text-white leading-tight">FIBERPERU E.I.R.L.</h1>
            <p className="text-xs text-slate-400">Sistema de Gestión & Seguimiento de Instalaciones</p>
          </div>
        </div>
        <div className="flex items-center gap-4">
          <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-medium bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
            <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span>
            Backend API Online
          </span>
          <button className="px-4 py-2 text-sm font-medium rounded-lg bg-cyan-600 hover:bg-cyan-500 text-white transition-all shadow-md shadow-cyan-600/30">
            Iniciar Sesión
          </button>
        </div>
      </header>

      {/* Hero Content */}
      <main className="flex-1 max-w-7xl w-full mx-auto px-6 py-10 space-y-10">
        <div className="bg-gradient-to-r from-cyan-900/30 via-slate-900 to-blue-900/20 rounded-2xl border border-cyan-500/20 p-8 relative overflow-hidden">
          <div className="relative z-10 max-w-3xl space-y-4">
            <span className="px-3 py-1 text-xs font-semibold uppercase tracking-wider bg-cyan-500/20 text-cyan-300 rounded-md border border-cyan-500/30">
              Proyecto Académico UTP - Curso Integrador II
            </span>
            <h2 className="text-3xl sm:text-4xl font-extrabold text-white leading-tight">
              Control Operativo & Trazabilidad de Dispositivos de Red en Tiempo Real
            </h2>
            <p className="text-slate-300 text-base leading-relaxed">
              Plataforma centralizada para la recepción de solicitudes, programación inteligente de técnicos, 
              control de inventario de equipos (routers, switches, ONT) y firma de conformidad digital con evidencias.
            </p>
          </div>
        </div>

        {/* Roles Quick Access (RBAC) */}
        <div>
          <h3 className="text-xl font-bold text-white mb-6 flex items-center gap-2">
            <span className="w-3 h-3 rounded-full bg-cyan-500"></span>
            Módulos por Rol de Usuario (RBAC)
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            {/* Admin */}
            <div className="bg-slate-900 border border-slate-800 hover:border-cyan-500/50 rounded-xl p-6 transition-all hover:-translate-y-1 shadow-lg group">
              <div className="w-12 h-12 rounded-lg bg-indigo-500/10 border border-indigo-500/20 flex items-center justify-center text-indigo-400 mb-4 group-hover:scale-110 transition-transform">
                ⚙️
              </div>
              <h4 className="font-bold text-lg text-white mb-2">Administrador</h4>
              <p className="text-xs text-slate-400 mb-4">
                Gestión total de usuarios, roles, catálogo de dispositivos, auditoría y reportes gerenciales.
              </p>
              <span className="text-xs font-medium text-cyan-400 group-hover:underline inline-flex items-center gap-1">
                Acceder al Panel &rarr;
              </span>
            </div>

            {/* Coordinador */}
            <div className="bg-slate-900 border border-slate-800 hover:border-cyan-500/50 rounded-xl p-6 transition-all hover:-translate-y-1 shadow-lg group">
              <div className="w-12 h-12 rounded-lg bg-cyan-500/10 border border-cyan-500/20 flex items-center justify-center text-cyan-400 mb-4 group-hover:scale-110 transition-transform">
                📋
              </div>
              <h4 className="font-bold text-lg text-white mb-2">Coordinador</h4>
              <p className="text-xs text-slate-400 mb-4">
                Recepción de solicitudes, generación de órdenes de trabajo y programación de fechas con técnicos.
              </p>
              <span className="text-xs font-medium text-cyan-400 group-hover:underline inline-flex items-center gap-1">
                Programar Órdenes &rarr;
              </span>
            </div>

            {/* Técnico */}
            <div className="bg-slate-900 border border-slate-800 hover:border-cyan-500/50 rounded-xl p-6 transition-all hover:-translate-y-1 shadow-lg group">
              <div className="w-12 h-12 rounded-lg bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center text-emerald-400 mb-4 group-hover:scale-110 transition-transform">
                🛠️
              </div>
              <h4 className="font-bold text-lg text-white mb-2">Técnico Instalador</h4>
              <p className="text-xs text-slate-400 mb-4">
                Consulta de hoja de ruta, vinculación de series de equipos y carga de fotos/evidencias.
              </p>
              <span className="text-xs font-medium text-cyan-400 group-hover:underline inline-flex items-center gap-1">
                Ver Órdenes Asignadas &rarr;
              </span>
            </div>

            {/* Cliente */}
            <div className="bg-slate-900 border border-slate-800 hover:border-cyan-500/50 rounded-xl p-6 transition-all hover:-translate-y-1 shadow-lg group">
              <div className="w-12 h-12 rounded-lg bg-amber-500/10 border border-amber-500/20 flex items-center justify-center text-amber-400 mb-4 group-hover:scale-110 transition-transform">
                📡
              </div>
              <h4 className="font-bold text-lg text-white mb-2">Cliente Solicitante</h4>
              <p className="text-xs text-slate-400 mb-4">
                Seguimiento en tiempo real del ticket de instalación y estado del servicio contratado.
              </p>
              <span className="text-xs font-medium text-cyan-400 group-hover:underline inline-flex items-center gap-1">
                Rastrear Ticket &rarr;
              </span>
            </div>
          </div>
        </div>

        {/* Live Metrics Showcase */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="bg-slate-900/60 border border-slate-800 rounded-xl p-5 flex items-center justify-between">
            <div>
              <p className="text-xs text-slate-400 uppercase tracking-wider">Órdenes Activas</p>
              <p className="text-2xl font-bold text-white mt-1">12 Pendientes</p>
            </div>
            <span className="w-10 h-10 rounded-full bg-cyan-500/10 text-cyan-400 flex items-center justify-center font-bold">12</span>
          </div>

          <div className="bg-slate-900/60 border border-slate-800 rounded-xl p-5 flex items-center justify-between">
            <div>
              <p className="text-xs text-slate-400 uppercase tracking-wider">Dispositivos en Stock</p>
              <p className="text-2xl font-bold text-emerald-400 mt-1">45 Equipos</p>
            </div>
            <span className="w-10 h-10 rounded-full bg-emerald-500/10 text-emerald-400 flex items-center justify-center font-bold">45</span>
          </div>

          <div className="bg-slate-900/60 border border-slate-800 rounded-xl p-5 flex items-center justify-between">
            <div>
              <p className="text-xs text-slate-400 uppercase tracking-wider">Técnicos en Ruta</p>
              <p className="text-2xl font-bold text-indigo-400 mt-1">8 Activos</p>
            </div>
            <span className="w-10 h-10 rounded-full bg-indigo-500/10 text-indigo-400 flex items-center justify-center font-bold">8</span>
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="border-t border-slate-800 bg-slate-900 px-6 py-6 text-center text-xs text-slate-500">
        © 2026 FIBERPERU E.I.R.L. - Sistema de Gestión de Instalaciones de Red (UTP)
      </footer>
    </div>
  );
}
