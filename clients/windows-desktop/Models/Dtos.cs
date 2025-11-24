using System;
using System.Text.Json.Serialization;

namespace NovelEverywhere.Desktop.Models;

public record LoginRequest(string Email, string Password);

public record TokenResponse([property: JsonPropertyName("access_token")] string AccessToken);

public record NovelDto(int Id, string Title, string Author, string Format, int Size, string? Description);

public record ProgressDto([property: JsonPropertyName("novel_id")] int NovelId, string Chapter, int Offset);

public record ReadingSettingsDto(
    [property: JsonPropertyName("font_family")] string FontFamily,
    [property: JsonPropertyName("font_size")] int FontSize,
    [property: JsonPropertyName("line_height")] int LineHeight,
    string Theme,
    [property: JsonPropertyName("bg_color")] string BgColor,
    [property: JsonPropertyName("tts_voice")] string TtsVoice);

public record TtsRequest(string Text, string? Voice = null, int? Rate = null);

public record TtsResponse([property: JsonPropertyName("audio_url")] string AudioUrl);
