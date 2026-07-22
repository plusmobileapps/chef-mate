import SwiftUI

/// Add an item via dictation / scribble (watchOS `TextField` presents both automatically).
struct AddItemView: View {
    let onAdd: (String) -> Void

    @Environment(\.dismiss) private var dismiss
    @State private var name = ""

    private var isValid: Bool { !name.trimmingCharacters(in: .whitespaces).isEmpty }

    var body: some View {
        NavigationStack {
            Form {
                TextField("Item", text: $name)
                Button {
                    onAdd(name)
                    dismiss()
                } label: {
                    Text("Add")
                        .fontWeight(.semibold)
                        .frame(maxWidth: .infinity)
                }
                .foregroundStyle(isValid ? WatchTheme.primary : WatchTheme.onSurfaceVariant)
                .disabled(!isValid)
            }
            .navigationTitle("Add Item")
        }
        .tint(WatchTheme.primary)
    }
}
