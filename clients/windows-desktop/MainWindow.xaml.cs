using System.Windows;
using NovelEverywhere.Desktop.ViewModels;

namespace NovelEverywhere.Desktop;

public partial class MainWindow : Window
{
    private readonly MainViewModel _viewModel = new();

    public MainWindow()
    {
        InitializeComponent();
        DataContext = _viewModel;
    }

    private void PasswordBox_OnPasswordChanged(object sender, RoutedEventArgs e)
    {
        if (sender is System.Windows.Controls.PasswordBox passwordBox)
        {
            _viewModel.Password = passwordBox.Password;
        }
    }
}
