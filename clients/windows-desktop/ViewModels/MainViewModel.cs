using System;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Speech.Synthesis;
using System.Threading.Tasks;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using NovelEverywhere.Desktop.Models;
using NovelEverywhere.Desktop.Services;

namespace NovelEverywhere.Desktop.ViewModels;

public partial class MainViewModel : ObservableObject
{
    private readonly NovelClient _client = new("http://localhost:8000/");
    private readonly SpeechSynthesizer _speechSynthesizer = new();

    public ObservableCollection<NovelDto> Novels { get; } = new();

    [ObservableProperty]
    private string _email = "";

    [ObservableProperty]
    private string _password = "";

    [ObservableProperty]
    private string _status = "请先登录";

    [ObservableProperty]
    private NovelDto? _selectedNovel;

    [ObservableProperty]
    private string _currentChapter = "开篇";

    [ObservableProperty]
    private string _readerContent = "选择小说后即可开始阅读。";

    [ObservableProperty]
    private bool _isBusy;

    public IAsyncRelayCommand LoginCommand { get; }
    public IAsyncRelayCommand RefreshCommand { get; }
    public IAsyncRelayCommand SyncCommand { get; }
    public IRelayCommand SpeakCommand { get; }

    public MainViewModel()
    {
        LoginCommand = new AsyncRelayCommand(LoginAsync);
        RefreshCommand = new AsyncRelayCommand(RefreshAsync);
        SyncCommand = new AsyncRelayCommand(SyncAsync);
        SpeakCommand = new RelayCommand(Speak);
    }

    partial void OnSelectedNovelChanged(NovelDto? value)
    {
        if (value is null) return;
        ReaderContent = $"《{value.Title}》\n\n云端内容将在此处展示。";
    }

    private async Task LoginAsync()
    {
        try
        {
            IsBusy = true;
            await _client.LoginAsync(Email, Password);
            Status = "登录成功，正在同步书架...";
            await RefreshAsync();
        }
        catch (Exception ex)
        {
            Status = $"登录失败：{ex.Message}";
        }
        finally
        {
            IsBusy = false;
        }
    }

    private async Task RefreshAsync()
    {
        try
        {
            IsBusy = true;
            Novels.Clear();
            var novels = await _client.GetNovelsAsync();
            foreach (var novel in novels)
            {
                Novels.Add(novel);
            }
            Status = $"已同步 {Novels.Count} 本小说";
        }
        catch (Exception ex)
        {
            Status = $"同步失败：{ex.Message}";
        }
        finally
        {
            IsBusy = false;
        }
    }

    private async Task SyncAsync()
    {
        if (SelectedNovel is null) return;
        try
        {
            IsBusy = true;
            await _client.UpdateProgressAsync(SelectedNovel.Id, new ProgressDto(SelectedNovel.Id, CurrentChapter, 0));
            Status = "阅读进度已上传";
        }
        catch (Exception ex)
        {
            Status = $"同步失败：{ex.Message}";
        }
        finally
        {
            IsBusy = false;
        }
    }

    private void Speak()
    {
        if (string.IsNullOrWhiteSpace(ReaderContent)) return;
        _speechSynthesizer.SpeakAsyncCancelAll();
        _speechSynthesizer.SpeakAsync(ReaderContent);
    }
}
