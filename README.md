# CutsZoom AI — AI Speech-Boundary Video Editor

CutsZoom AI is a production-quality, frame-accurate vertical video editing application for Android powered by Jetpack Compose and Gemini AI. It analyzes spoken audio cadence, transcribes speech with word-level timestamps, detects natural phrase and sentence completion boundaries, and automatically applies calibrated wide-frame (0.70x) to normal-frame (1.00x) zoom cuts using deterministic cubic ease-out keyframe curves.

---

## What the Application Does

Instead of manually cutting video clips and hand-animating camera zoom keyframes, CutsZoom AI automates the signature vertical video pacing style used by top creators:

1. **User Uploads Video:** User selects any video from the device gallery via the zero-permission Photo Picker or tests with the built-in calibrated vertical reference clip.
2. **Media Inspection:** The engine extracts presentation metadata (exact FPS, resolution, rotation, bitrate, channel count, and sample rate).
3. **Audio Extraction & Waveform Generation:** Computes RMS acoustic energy buckets across time to visualize real-time vocal cadence.
4. **Speech Transcription & Word Timestamps:** Generates speech words with start/end millisecond timestamps and confidence scores.
5. **Semantic Phrase Boundary Detection:** Gemini analyzes thought completion, grammatical clauses, punctuation, and acoustic pauses. The offline linguistic engine acts as a fallback to ensure 100% reliable local detection.
6. **Boundary Confidence Scoring:** Every candidate cut receives a multi-dimensional score across:
   - `pauseScore` (acoustic silence gap)
   - `punctuationScore` (syntactic sentence enders)
   - `semanticCompletionScore` (clause completeness & conjunction avoidance)
   - `rhythmScore` (vocal cadence inflection)
   - `speechBoundaryScore` (combined metric weight)
7. **Frame-Accurate Keyframe Curves:** Converts every approved boundary into exact source video frames (`frameIndex = round(boundaryTime * sourceFPS)`), applying the calibrated zoom profile:
   - $F - 1 \to 1.00\times$ (normal scale)
   - $F \to 0.70\times$ (calibrated wide scale)
   - $F + 2 \to 0.75\times$
   - $F + 4 \to 0.82\times$
   - $F + 6 \to 0.90\times$
   - $F + 8 \to 0.96\times$
   - $F + 10 \to 1.00\times$ (smooth return to normal framing)
8. **Real-Time Interactive Preview:** Plays video in a 9:16 viewport with live hardware canvas scaling, HUD readouts (`0.70x WIDE` / `1.00x NORMAL`), timecode, frame index counter, and an interactive multi-track timeline.
9. **Interactive Keyframe Inspector:** Allows adjusting wide scale (0.50x to 0.90x), recovery duration (6 to 24 frames), split position, or deleting and adding custom splits at the playhead.
10. **Hardware-Accelerated MP4 Export:** Renders the edited video using Android's native `MediaCodec` (H.264), `MediaExtractor`, and `MediaMuxer`. The master audio timeline is preserved losslessly without pitch distortion, drift, or desync.
11. **Automated Validation:** Performs post-render verification of container integrity, audio/video duration match, and absence of corrupted frames before presenting the shareable file.

---

## Core Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    CutsZoom AI Engine                       │
└─────────────────────────────────────────────────────────────┘
                               │
            ┌──────────────────┴──────────────────┐
            ▼                                     ▼
┌───────────────────────┐             ┌───────────────────────┐
│ Media & Audio Extract │             │  Gemini AI & Cadence  │
│  - MediaInspector     │             │  - Semantic analysis  │
│  - WaveformExtractor  │             │  - Speech transcription│
│  - MediaMetadata      │             │  - Word timestamps    │
└───────────────────────┘             └───────────────────────┘
            │                                     │
            └──────────────────┬──────────────────┘
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                   KeyframeEngine & Timeline                 │
│  - Frame-accurate boundary conversion: round(T * FPS)        │
│  - Calibrated keyframe sequence: -1, 0, +2, +4, +6, +8, +10  │
│  - Cubic ease-out interpolation: f(u) = 1 - (1 - u)^3       │
│  - Canonical EditTimeline JSON single source of truth        │
└─────────────────────────────────────────────────────────────┘
                               │
            ┌──────────────────┴──────────────────┐
            ▼                                     ▼
┌───────────────────────┐             ┌───────────────────────┐
│  Interactive Studio   │             │   VideoExportEngine   │
│  - VideoPreviewCanvas │             │  - Hardware MediaCodec│
│  - Multi-track Ruler  │             │  - Pristine Audio Mux │
│  - Keyframe Inspector │             │  - Container Validator│
└───────────────────────┘             └───────────────────────┘
```

---

## Calibrated Keyframe Specification

The zoom model is calibrated from the 48.576s vertical reference video (24.024 FPS):

- **Normal scale:** $1.00\times$
- **Wide scale:** $0.70\times$
- **Keyframe offsets:** `[-1, 0, 2, 4, 6, 8, 10]`
- **Keyframe scales:** `[1.00, 0.70, 0.75, 0.82, 0.90, 0.96, 1.00]`
- **Interpolation:** Smooth cubic ease-out: $f(t) = 1.0 - (1.0 - t)^3$
- **Origin:** Center framing ($X = 0.50$, $Y = 0.50$)
- **Minimum Segment Rule:** Default threshold of 0.50s–0.70s with dynamic adaptation for rapid speech rhythm.

---

## Environment Variables Configuration

API keys and secrets are securely handled via the Secrets Gradle Plugin:

1. Copy `.env.example` to `.env`:
   ```bash
   cp .env.example .env
   ```
2. Populate `.env` with your Google Gemini API key:
   ```env
   GEMINI_API_KEY=your_actual_gemini_api_key_here
   ```
3. In Google AI Studio, keys are configured securely in the **Secrets panel** and injected at build time into `BuildConfig.GEMINI_API_KEY`.

> **Note:** `.env` is listed in `.gitignore` and is never committed to GitHub. If no API key is set, the app continues to operate using its built-in acoustic and linguistic boundary engine.

---

## How to Build and Run Locally

### Prerequisites
- Android Studio Ladybug / Meerkat or newer
- JDK 17 or JDK 21
- Android SDK Platform 36 (Android 14 / 15 / 16)
- Gradle 8.11+ / AGP 9.1.1

### Build APK
```bash
gradle assembleDebug
```

### Run Unit & Robolectric Tests
```bash
gradle :app:testDebugUnitTest
```

### Install onto Device / Emulator
```bash
gradle installDebug
```

---

## Video Processing & Hardware Acceleration

- **Decoding & Scaling:** Utilizes `android.media.MediaCodec` and `android.media.MediaExtractor` for hardware-accelerated video decoding.
- **Frame-Accurate Matrix Transformation:** Centers and scales decoded frames on an OpenGL ES / Hardware Canvas surface according to the deterministic keyframe curve evaluated at the presentation timestamp (`ptsUs`).
- **Muxing & Audio Synchronization:** `android.media.MediaMuxer` recombines the encoded H.264 video stream with the untouched source audio stream (AAC/PCM), preventing any drift or re-encoding artifacts.
- **Horizontal Video Adaption:** For non-vertical videos, the engine automatically composites the centered scaled foreground over a softly dimmed background, ensuring true 9:16 vertical delivery.

---

## Troubleshooting

- **Google Services warning:** `googleServices { missingGoogleServicesStrategy = MissingGoogleServicesStrategy.WARN }` is enabled so the app compiles cleanly whether or not `google-services.json` is provided.
- **Audio extraction permissions:** The app uses Android's zero-permission Photo Picker (`PickVisualMedia`), eliminating the need for broad storage permissions.
- **Gemini API Key missing:** If `BuildConfig.GEMINI_API_KEY` is not provided, the app seamlessly defaults to its local acoustic energy and cadence analyzer. Users can also enter a custom key in the Project tab.

---

## License

Apache License 2.0. Built with Google AI Studio and Jetpack Compose.
