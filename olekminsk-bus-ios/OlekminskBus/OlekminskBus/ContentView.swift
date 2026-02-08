import SwiftUI

struct ContentView: View {
    @StateObject private var viewModel = BusViewModel()
    @State private var selectedTab = 0
    
    var body: some View {
        TabView(selection: $selectedTab) {
            RoutesView(viewModel: viewModel)
                .tabItem {
                    Image(systemName: "bus")
                    Text("Маршруты")
                }
                .tag(0)
            
            SuburbanView(viewModel: viewModel)
                .tabItem {
                    Image(systemName: "mappin.and.ellipse")
                    Text("Пригород")
                }
                .tag(1)
            
            FavoritesView(viewModel: viewModel)
                .tabItem {
                    Image(systemName: "heart.fill")
                    Text("Избранное")
                }
                .tag(2)
            
            SettingsView()
                .tabItem {
                    Image(systemName: "gear")
                    Text("Настройки")
                }
                .tag(3)
        }
        .accentColor(.purple)
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
