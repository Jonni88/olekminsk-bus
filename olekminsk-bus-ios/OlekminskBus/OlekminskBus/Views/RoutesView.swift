import SwiftUI

struct RoutesView: View {
    @ObservedObject var viewModel: BusViewModel
    @State private var selectedRoute: BusRoute?
    
    var body: some View {
        NavigationView {
            List(viewModel.routes.filter { $0.type == .urban }) { route in
                RouteRow(route: route, viewModel: viewModel)
                    .contentShape(Rectangle())
                    .onTapGesture {
                        selectedRoute = route
                    }
            }
            .listStyle(PlainListStyle())
            .navigationTitle("Маршруты")
            .sheet(item: $selectedRoute) { route in
                RouteDetailView(route: route, viewModel: viewModel)
            }
        }
    }
}

struct RouteRow: View {
    let route: BusRoute
    @ObservedObject var viewModel: BusViewModel
    
    var body: some View {
        HStack {
            // Номер маршрута
            ZStack {
                RoundedRectangle(cornerRadius: 12)
                    .fill(routeTypeColor.opacity(0.2))
                    .frame(width: 56, height: 56)
                
                Text(route.number)
                    .font(.system(size: 20, weight: .bold))
                    .foregroundColor(routeTypeColor)
            }
            
            VStack(alignment: .leading, spacing: 4) {
                Text(route.name)
                    .font(.system(size: 16, weight: .medium))
                
                Text(route.type.displayName)
                    .font(.system(size: 12, weight: .medium))
                    .foregroundColor(routeTypeColor)
            }
            
            Spacer()
            
            Button(action: {
                viewModel.toggleFavorite(routeId: route.id)
            }) {
                Image(systemName: viewModel.isFavorite(routeId: route.id) ? "heart.fill" : "heart")
                    .foregroundColor(viewModel.isFavorite(routeId: route.id) ? .red : .gray)
                    .font(.system(size: 22))
            }
        }
        .padding(.vertical, 8)
    }
    
    var routeTypeColor: Color {
        switch route.type {
        case .urban: return .blue
        case .suburban: return .green
        case .intercity: return .orange
        }
    }
}

struct RoutesView_Previews: PreviewProvider {
    static var previews: some View {
        RoutesView(viewModel: BusViewModel())
    }
}
