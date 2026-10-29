import React, { useState } from 'react';
import {
  Mail,
  Key,
  User,
  Send,
  Download,
  LogOut,
  ShieldCheck,
  CheckCircle2,
  FolderArchive,
  Code2,
} from 'lucide-react';
import { EmailSettings } from '../types';
import { downloadAndroidProjectZip } from '../utils/zipExport';

interface SettingsScreenProps {
  currentOwnerEmail: string;
  emailSettings: EmailSettings;
  onSaveEmailSettings: (settings: EmailSettings) => void;
  onSendTestEmail: (onResult: (success: boolean, msg: string) => void) => void;
  onExportBackupJson: () => void;
  onSignOut: () => void;
}

export const SettingsScreen: React.FC<SettingsScreenProps> = ({
  currentOwnerEmail,
  emailSettings,
  onSaveEmailSettings,
  onSendTestEmail,
  onExportBackupJson,
  onSignOut,
}) => {
  const [gmailAddress, setGmailAddress] = useState(emailSettings.gmailAddress || 'mistryjay1804p@gmail.com');
  const [appPassword, setAppPassword] = useState('');
  const [senderDisplayName, setSenderDisplayName] = useState(emailSettings.senderDisplayName || 'Travel Dispatcher');
  const [replyToEmail, setReplyToEmail] = useState(emailSettings.replyToEmail || '');

  const [hasPasswordSaved, setHasPasswordSaved] = useState(emailSettings.hasAppPasswordSaved ?? true);
  const [saveSuccessMsg, setSaveSuccessMsg] = useState<string | null>(null);

  const [isSendingTest, setIsSendingTest] = useState(false);
  const [testResult, setTestResult] = useState<{ success: boolean; msg: string } | null>(null);

  const [isExportingZip, setIsExportingZip] = useState(false);

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    const finalPassword = appPassword.trim() || emailSettings.appPassword || '';
    onSaveEmailSettings({
      gmailAddress: gmailAddress.trim(),
      appPassword: finalPassword,
      hasAppPasswordSaved: Boolean(finalPassword),
      senderDisplayName: senderDisplayName.trim() || 'Travel Dispatcher',
      replyToEmail: replyToEmail.trim(),
    });

    setHasPasswordSaved(Boolean(finalPassword));
    setAppPassword(''); // Clear input for security
    setSaveSuccessMsg('Email settings saved securely. Ready to send emails over smtp.gmail.com:465 SSL.');
    setTimeout(() => setSaveSuccessMsg(null), 4000);
  };

  const handleTestEmail = () => {
    const passwordToUse = appPassword.trim() || emailSettings.appPassword || '';
    if (!gmailAddress.trim() || !passwordToUse) {
      setTestResult({
        success: false,
        msg: 'Please enter your Gmail address and 16-character App Password.',
      });
      return;
    }

    // If user typed a new password into the input, save it immediately
    if (appPassword.trim()) {
      onSaveEmailSettings({
        gmailAddress: gmailAddress.trim(),
        appPassword: appPassword.trim(),
        hasAppPasswordSaved: true,
        senderDisplayName: senderDisplayName.trim() || 'Travel Dispatcher',
        replyToEmail: replyToEmail.trim(),
      });
      setHasPasswordSaved(true);
    }

    setIsSendingTest(true);
    setTestResult(null);

    onSendTestEmail((success, msg) => {
      setIsSendingTest(false);
      setTestResult({ success, msg });
    });
  };

  const handleDownloadZip = async () => {
    setIsExportingZip(true);
    try {
      await downloadAndroidProjectZip();
    } finally {
      setIsExportingZip(false);
    }
  };

  return (
    <div className="mx-auto max-w-xl pb-24 pt-3">
      {/* Title & Subtitle */}
      <div className="px-4">
        <h1 className="text-xl font-bold tracking-tight text-slate-900">
          Settings &amp; Email Setup
        </h1>
        <p className="mt-0.5 text-xs text-slate-500">
          Manage owner session, Gmail SMTP credentials, and data exports
        </p>
      </div>

      <div className="mt-4 space-y-4 px-4">
        {/* 1. SIGNED-IN ACCOUNT CARD */}
        <div className="rounded-2xl border border-slate-200/90 bg-white p-4.5 shadow-xs">
          <div className="flex items-center justify-between">
            <div>
              <span className="text-[11px] font-bold tracking-wider text-slate-500 block">
                SIGNED-IN OWNER ACCOUNT
              </span>
              <p className="mt-1 text-sm font-bold text-slate-900">{currentOwnerEmail}</p>
            </div>

            <button
              onClick={onSignOut}
              className="flex items-center gap-1.5 rounded-xl border border-rose-200 bg-rose-50 px-3 py-2 text-xs font-semibold text-rose-600 hover:bg-rose-100 transition-colors"
            >
              <LogOut className="h-3.5 w-3.5" />
              <span>Sign out</span>
            </button>
          </div>
        </div>

        {/* 2. EMAIL ACCOUNT (GMAIL SMTP + KEYSTORE) */}
        <div className="rounded-2xl border border-slate-200/90 bg-white p-4.5 shadow-xs">
          <label className="text-[11px] font-bold tracking-wider text-slate-500 block">
            EMAIL ACCOUNT (GMAIL SMTP &middot; PORT 465 SSL)
          </label>
          <p className="mt-1 text-xs text-slate-500 leading-relaxed">
            Quotations are dispatched directly from the device to partner travel agents over direct SSL on port 465. Passwords are saved with hardware-backed encryption (Android Keystore) and never exposed.
          </p>

          <form onSubmit={handleSave} className="mt-4 space-y-3.5">
            <div>
              <label className="block text-xs font-semibold text-slate-800">
                Gmail Address <span className="text-rose-500">*</span>
              </label>
              <div className="relative mt-1">
                <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3.5 text-slate-400">
                  <Mail className="h-4 w-4" />
                </div>
                <input
                  type="email"
                  required
                  value={gmailAddress}
                  onChange={(e) => setGmailAddress(e.target.value)}
                  placeholder="e.g. yourname@gmail.com"
                  className="w-full rounded-xl border border-slate-200 py-2.5 pl-10 pr-3.5 text-xs text-slate-900 focus:border-slate-900 focus:outline-none"
                />
              </div>
            </div>

            <div>
              <div className="flex items-center justify-between">
                <label className="text-xs font-semibold text-slate-800">
                  Gmail App Password <span className="text-rose-500">*</span>
                </label>
                {hasPasswordSaved && (
                  <span className="flex items-center gap-1 text-[11px] font-semibold text-emerald-600">
                    <ShieldCheck className="h-3.5 w-3.5" />
                    Encrypted &amp; Stored
                  </span>
                )}
              </div>
              <div className="relative mt-1">
                <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3.5 text-slate-400">
                  <Key className="h-4 w-4" />
                </div>
                <input
                  type="password"
                  value={appPassword}
                  onChange={(e) => setAppPassword(e.target.value)}
                  placeholder={
                    hasPasswordSaved
                      ? '•••••••••••••••• (Leave blank to keep saved password)'
                      : '16-character App Password (e.g. abcd efgh ijkl mnop)'
                  }
                  className="w-full rounded-xl border border-slate-200 py-2.5 pl-10 pr-3.5 text-xs text-slate-900 focus:border-slate-900 focus:outline-none"
                />
              </div>
              <p className="mt-1 text-[11px] text-slate-400">
                Created in Google Account &gt; Security &gt; 2-Step Verification &gt; App passwords.
              </p>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-800">
                Sender Display Name
              </label>
              <div className="relative mt-1">
                <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3.5 text-slate-400">
                  <User className="h-4 w-4" />
                </div>
                <input
                  type="text"
                  value={senderDisplayName}
                  onChange={(e) => setSenderDisplayName(e.target.value)}
                  placeholder="e.g. Travel Dispatcher / Jaykumar Mistry"
                  className="w-full rounded-xl border border-slate-200 py-2.5 pl-10 pr-3.5 text-xs text-slate-900 focus:border-slate-900 focus:outline-none"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-800">
                Reply-To Address (Optional)
              </label>
              <div className="relative mt-1">
                <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3.5 text-slate-400">
                  <Mail className="h-4 w-4" />
                </div>
                <input
                  type="email"
                  value={replyToEmail}
                  onChange={(e) => setReplyToEmail(e.target.value)}
                  placeholder="e.g. quotes@yourcompany.com"
                  className="w-full rounded-xl border border-slate-200 py-2.5 pl-10 pr-3.5 text-xs text-slate-900 focus:border-slate-900 focus:outline-none"
                />
              </div>
            </div>

            {saveSuccessMsg && (
              <p className="text-xs font-medium text-emerald-700 bg-emerald-50 border border-emerald-200 rounded-xl p-2.5 flex items-center gap-1.5">
                <CheckCircle2 className="h-4 w-4 text-emerald-600 shrink-0" />
                <span>{saveSuccessMsg}</span>
              </p>
            )}

            <div className="flex items-center gap-2.5 pt-2">
              <button
                type="submit"
                className="flex-1 rounded-xl bg-slate-900 py-2.5 text-xs font-bold text-white shadow-sm hover:bg-slate-800 transition-colors"
              >
                Save Email Settings
              </button>

              <button
                type="button"
                onClick={handleTestEmail}
                disabled={isSendingTest || !gmailAddress}
                className="flex items-center gap-1.5 rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-xs font-bold text-slate-800 hover:bg-slate-50 transition-colors disabled:opacity-50"
              >
                {isSendingTest ? (
                  <span className="flex items-center gap-1.5">
                    <span className="h-3.5 w-3.5 animate-spin rounded-full border-2 border-slate-700 border-t-transparent" />
                    Testing...
                  </span>
                ) : (
                  <>
                    <Send className="h-3.5 w-3.5" />
                    <span>Send test email to myself</span>
                  </>
                )}
              </button>
            </div>

            {testResult && (
              <p
                className={`text-xs font-medium rounded-xl p-2.5 ${
                  testResult.success
                    ? 'text-emerald-700 bg-emerald-50 border border-emerald-200'
                    : 'text-rose-700 bg-rose-50 border border-rose-200'
                }`}
              >
                {testResult.msg}
              </p>
            )}
          </form>
        </div>

        {/* 3. EXPORT BACKUP */}
        <div className="rounded-2xl border border-slate-200/90 bg-white p-4.5 shadow-xs">
          <label className="text-[11px] font-bold tracking-wider text-slate-500 block">
            DATA BACKUP
          </label>
          <p className="mt-1 text-xs text-slate-500 leading-relaxed">
            Export a full JSON backup of all inquiries, stops, dispatches, middleman ledger records, and agent directory data.
          </p>

          <button
            type="button"
            onClick={onExportBackupJson}
            className="mt-3.5 flex items-center gap-2 rounded-xl bg-slate-900 px-4 py-2.5 text-xs font-bold text-white shadow-sm hover:bg-slate-800 transition-colors"
          >
            <Download className="h-4 w-4" />
            <span>Export Backup (JSON)</span>
          </button>
        </div>

        {/* 4. ANDROID STUDIO PROJECT EXPORT */}
        <div className="rounded-2xl border border-slate-200/90 bg-white p-4.5 shadow-xs">
          <label className="text-[11px] font-bold tracking-wider text-slate-500 block">
            NATIVE ANDROID PROJECT (KOTLIN &middot; JETPACK COMPOSE)
          </label>
          <p className="mt-1 text-xs text-slate-500 leading-relaxed">
            The repository contains the complete native Android Studio codebase (minSdk 26, Material 3, supabase-kt, JavaMail, Keystore AES256-GCM, unit tests, and PostgreSQL migrations).
          </p>

          <button
            type="button"
            onClick={handleDownloadZip}
            disabled={isExportingZip}
            className="mt-3.5 flex items-center gap-2 rounded-xl border border-slate-300 bg-white px-4 py-2.5 text-xs font-bold text-slate-800 hover:bg-slate-50 transition-colors"
          >
            <FolderArchive className="h-4 w-4 text-slate-600" />
            <span>
              {isExportingZip ? 'Creating ZIP...' : 'Download Android Studio Project (.ZIP)'}
            </span>
          </button>
        </div>

        {/* 5. CREDITS (Jaykumar Mistry only, as instructed) */}
        <div className="pt-2 text-center">
          <p className="text-xs font-medium text-slate-400">
            Travel Dispatcher &middot; Developed for Jaykumar Mistry
          </p>
        </div>
      </div>
    </div>
  );
};
