let tagCatalogPromise;
let cityCatalogPromise;

// Returns the UI label for either a structured tag or a legacy string tag.
export function formatTagLabel(tag) {
  if (!tag) return "";
  if (typeof tag === "string") return tag;
  return tag.microTag ? `${tag.macroTag} > ${tag.microTag}` : tag.macroTag;
}

// Compares two tags by macro and micro tag, treating missing micro tags as null.
export function sameTag(a, b) {
  return a?.macroTag === b?.macroTag && (a?.microTag || null) === (b?.microTag || null);
}

// Loads the hierarchical tag catalog once and flattens it for autocomplete and filters.
export function loadTagOptions() {
  if (!tagCatalogPromise) {
    tagCatalogPromise = fetch("/data/tag_corsi_gerarchici.json")
      .then((res) => {
        if (!res.ok) throw new Error("Errore caricamento tag");
        return res.json();
      })
      .then((catalog) =>
        Object.entries(catalog).flatMap(([macroTag, microTags]) => [
          { macroTag, microTag: null, label: macroTag, isMacro: true },
          ...microTags.map((microTag) => ({
            macroTag,
            microTag,
            label: `${macroTag} > ${microTag}`,
            isMacro: false,
          })),
        ])
      );
  }
  return tagCatalogPromise;
}

// Loads the city catalog once and prepares readable labels like "Milano (MI)".
export function loadCityOptions() {
  if (!cityCatalogPromise) {
    cityCatalogPromise = fetch("/data/gi_comuni.json")
      .then((res) => {
        if (!res.ok) throw new Error("Errore caricamento comuni");
        return res.json();
      })
      .then((cities) =>
        cities.map((city) => ({
          name: city.denominazione_ita,
          province: city.sigla_provincia,
          label: `${city.denominazione_ita} (${city.sigla_provincia})`,
          latitude: Number(city.lat),
          longitude: Number(city.lon),
        }))
      );
  }
  return cityCatalogPromise;
}
