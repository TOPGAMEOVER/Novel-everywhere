using System;
using System.Collections.Generic;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Threading.Tasks;
using NovelEverywhere.Desktop.Api;
using NovelEverywhere.Desktop.Models;
using Refit;

namespace NovelEverywhere.Desktop.Services;

public class NovelClient
{
    private readonly HttpClient _httpClient;
    private readonly INovelApi _api;
    private string? _token;

    public NovelClient(string baseUrl)
    {
        _httpClient = new HttpClient
        {
            BaseAddress = new Uri(baseUrl),
        };
        _api = RestService.For<INovelApi>(_httpClient);
    }

    public async Task LoginAsync(string email, string password)
    {
        var token = await _api.LoginAsync(new LoginRequest(email, password));
        _token = token.AccessToken;
        _httpClient.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", _token);
    }

    public Task<List<NovelDto>> GetNovelsAsync() => _api.GetNovelsAsync();

    public Task<ProgressDto> GetProgressAsync(int novelId) => _api.GetProgressAsync(novelId);

    public Task<ProgressDto> UpdateProgressAsync(int novelId, ProgressDto progress) => _api.UpdateProgressAsync(novelId, progress);

    public Task<ReadingSettingsDto> GetSettingsAsync() => _api.GetSettingsAsync();

    public Task<TtsResponse> CreateTtsAsync(string text) => _api.CreateTtsAsync(new TtsRequest(text));
}
