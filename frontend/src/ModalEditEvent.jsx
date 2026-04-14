import React, { useState } from "react";

export default function ModalEditEvent({ event, onClose, onSuccess }) {
  const [titolo, setTitolo] = useState(event.titolo || "");
  const [descrizione, setDescrizione] = useState(event.descrizione || "");
  const [dataOra, setDataOra] = useState(
    event.dataOra ? event.dataOra.slice(0, 16) : ""
  );
  const [luogo, setLuogo] = useState(event.luogo || "");
  const [durata, setDurata] = useState(event.durata || "");
  const [maxPartecipanti, setMaxPartecipanti] = useState(event.maxPartecipanti || "");
  const [partecipazioneCreatoreNecessaria, setPartecipazioneCreatoreNecessaria] =
    useState(event.partecipazioneCreatoreNecessaria || false);

  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [tagInput, setTagInput] = useState(event.tag ? event.tag.join(", ") : "");
  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError("");
    const tag = tagInput
    .split(",")
    .map((t) => t.trim())
    .filter((t) => t.length > 0);
    try {
      const response = await fetch(`http://localhost:8080/Eventi/${event.id}`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
        },
        credentials: "include",
        body: JSON.stringify({
          titolo,
          descrizione,
          dataOra,
          luogo,
          durata: Number(durata),
          maxPartecipanti: Number(maxPartecipanti),
          partecipazioneCreatoreNecessaria,
          tag,
        }),
      });

      if (!response.ok) {
        const message = await response.text();
        throw new Error(message || "Errore nella modifica dell'evento");
      }

      onSuccess();
    } catch (err) {
      setError(err.message || "Errore nella modifica dell'evento");
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
      <div className="bg-white w-full max-w-2xl rounded-xl shadow-lg p-6">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-xl font-bold text-gray-900">Modifica evento</h2>
          <button
            onClick={onClose}
            className="text-gray-500 hover:text-gray-700 text-xl"
            type="button"
          >
            ×
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block font-medium mb-1">Titolo</label>
            <input
              type="text"
              value={titolo}
              onChange={(e) => setTitolo(e.target.value)}
              className="w-full border rounded-lg px-3 py-2"
              required
            />
          </div>

          <div>
            <label className="block font-medium mb-1">Descrizione</label>
            <textarea
              value={descrizione}
              onChange={(e) => setDescrizione(e.target.value)}
              className="w-full border rounded-lg px-3 py-2"
              rows={4}
              required
            />
          </div>
          <div>
            <label className="block font-medium mb-1">Tag</label>
            <input
              type="text"
              value={tagInput}
              onChange={(e) => setTagInput(e.target.value)}
              className="w-full border rounded-lg px-3 py-2"
              placeholder="es. Informatica, Matematica"
              required
            />
          </div>

          <div>
            <label className="block font-medium mb-1">Data e ora</label>
            <input
              type="datetime-local"
              value={dataOra}
              onChange={(e) => setDataOra(e.target.value)}
              className="w-full border rounded-lg px-3 py-2"
              required
            />
          </div>

          <div>
            <label className="block font-medium mb-1">Luogo</label>
            <input
              type="text"
              value={luogo}
              onChange={(e) => setLuogo(e.target.value)}
              className="w-full border rounded-lg px-3 py-2"
              required
            />
          </div>

          <div>
            <label className="block font-medium mb-1">Durata (ore)</label>
            <input
              type="number"
              min="0.5"
              step="0.5"
              value={durata}
              onChange={(e) => setDurata(e.target.value)}
              className="w-full border rounded-lg px-3 py-2"
              required
            />
          </div>

          <div>
            <label className="block font-medium mb-1">Numero massimo partecipanti</label>
            <input
              type="number"
              min="1"
              value={maxPartecipanti}
              onChange={(e) => setMaxPartecipanti(e.target.value)}
              className="w-full border rounded-lg px-3 py-2"
              required
            />
          </div>

          <label className="flex items-center gap-2">
            <input
              type="checkbox"
              checked={partecipazioneCreatoreNecessaria}
              onChange={(e) =>
                setPartecipazioneCreatoreNecessaria(e.target.checked)
              }
            />
            <span>Partecipazione del creatore necessaria</span>
          </label>

          {error && (
            <div className="text-red-600 text-sm font-medium">{error}</div>
          )}

          <div className="flex justify-end gap-3 pt-2">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 rounded-lg border"
              disabled={saving}
            >
              Annulla
            </button>
            <button
              type="submit"
              className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 disabled:opacity-60"
              disabled={saving}
            >
              {saving ? "Salvataggio..." : "Salva modifiche"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}