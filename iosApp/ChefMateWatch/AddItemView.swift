import SwiftUI

/// Add an item via dictation / scribble (watchOS `TextField` presents both automatically).
struct AddItemView: View {
    let onAdd: (String) -> Void

    @Environment(\.dismiss) private var dismiss
    @State private var name = ""

    var body: some View {
        NavigationStack {
            Form {
                TextField("Item", text: $name)
                Button("Add") {
                    onAdd(name)
                    dismiss()
                }
                .disabled(name.trimmingCharacters(in: .whitespaces).isEmpty)
            }
            .navigationTitle("Add Item")
        }
    }
}
