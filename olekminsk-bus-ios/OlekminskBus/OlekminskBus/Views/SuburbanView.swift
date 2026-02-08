import SwiftUI

struct SuburbanView: View {
    @ObservedObject var viewModel: BusViewModel
    @State private var selectedRoute: BusRoute?
    
    var body: some View {
        NavigationView {
            Group {
                if viewModel.suburbanRoutes.isEmpty {
                    EmptyStateView()
                } else {
                    List(viewModel.suburbanRoutes) { route in
                        RouteRow(route: route, viewModel: viewModel)
                            .contentShape(Rectangle())
                            .onTapGesture {
                                selectedRoute = route
                            }
                    }
                    .listStyle(PlainListStyle())
                }
            }
            .navigationTitle("Пригород")
            .sheet(item: $selectedRoute) { route in
                RouteDetailView(route: route, viewModel: viewModel)
            }
        }
    }
}

struct EmptyStateView: View {
    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: "mappin.slash")
                .font(.system(size: 64))
                .foregroundColor(.gray)
            
            Text("Нет пригородных маршрутов")
                .font(.headline)
                .foregroundColor(.gray)
            
            Text("Проверьте подключение к интернету\nили обновите расписание")
                .font(.subheadline)
                .foregroundColor(.gray.opacity(0.7))
                .multilineTextAlignment(.center)
        }
        .padding()
    }
}

struct SuburbanView_Previews: PreviewProvider {
    static var previews: some View {
        SuburbanView(viewModel: BusViewModel())
    }
}
