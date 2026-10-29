// Export complete Android Studio Project with all Kotlin source files & preconfigured Supabase credentials
export async function downloadAndroidProjectZip() {
  try {
    const res = await fetch('/api/download-android-zip');
    if (!res.ok) {
      throw new Error(`Failed to download zip: ${res.statusText}`);
    }
    const blob = await res.blob();
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `travel-dispatcher-android-project-${Date.now()}.zip`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  } catch (err) {
    console.error('Download error, falling back to direct navigation:', err);
    window.location.href = '/api/download-android-zip';
  }
}
