using System.Collections.Generic;
using System.Threading.Tasks;
using NovelEverywhere.Desktop.Models;
using Refit;

namespace NovelEverywhere.Desktop.Api;

public interface INovelApi
{
    [Post("/auth/login")]
    Task<TokenResponse> LoginAsync([Body] LoginRequest request);

    [Get("/novels")]
    Task<List<NovelDto>> GetNovelsAsync();

    [Get("/novels/{id}/progress")]
    Task<ProgressDto> GetProgressAsync(int id);

    [Post("/novels/{id}/progress")]
    Task<ProgressDto> UpdateProgressAsync(int id, [Body] ProgressDto progress);

    [Get("/settings/reading")]
    Task<ReadingSettingsDto> GetSettingsAsync();

    [Post("/tts")]
    Task<TtsResponse> CreateTtsAsync([Body] TtsRequest request);
}
