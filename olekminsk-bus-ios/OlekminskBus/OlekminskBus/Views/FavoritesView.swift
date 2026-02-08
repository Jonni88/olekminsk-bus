import SwiftUI

struct FavoritesView: View {
    @ObservedObject var viewModel: BusViewModel
    @State private var selectedRoute: BusRoute?
    
    var body: some View {
        NavigationView {
            Group {
                if viewModel.favoriteRoutes.isEmpty {
                    EmptyFavoritesView()
                } else {
                    List(viewModel.favoriteRoutes) { route in
                        RouteRow(route: route, viewModel: viewModel)
                            .contentShape(Rectangle())
                            .onTapGesture {
                                selectedRoute = route
                            }
                    }
                    .listStyle(PlainListStyle())
                }
            }
            .navigationTitle("Избранное")
            .sheet(item: $selectedRoute) { route in
                RouteDetailView(route: route, viewModel: viewModel)
            }
        }
    }
}

struct EmptyFavoritesView: View {
    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: "heart.slash")
                .font(.system(size: 64))
                .foregroundColor(.gray)
            
            Text("Нет избранных маршрутов")
                .font(.headline)
                .foregroundColor(.gray)
            
            Text("Нажмите ♡ на маршруте,\nчтобы добавить сюда")
                .font(.subheadline)
                .foregroundColor(.gray.opacity(0.7))
                .multilineTextAlignment(.center)
        }
        .padding()
    }
}

struct FavoritesView_Previews: PreviewProvider {
    static var previews: some View {
        FavoritesView(viewModel: BusViewModel())
    }
}
