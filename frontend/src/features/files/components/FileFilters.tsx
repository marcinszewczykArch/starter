interface FileFiltersProps {
  contentType: string | null;
  search: string | null;
  onContentTypeChange: (contentType: string | null) => void;
  onSearchChange: (search: string) => void;
}

export function FileFilters({
  contentType,
  search,
  onContentTypeChange,
  onSearchChange,
}: FileFiltersProps) {
  return (
    <div className="flex gap-4 mb-6">
      {/* Search */}
      <div className="flex-1">
        <input
          type="text"
          placeholder="Search files..."
          value={search || ''}
          onChange={(e) => onSearchChange(e.target.value || null)}
          className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
        />
      </div>

      {/* Content Type Filter */}
      <select
        value={contentType || ''}
        onChange={(e) => onContentTypeChange(e.target.value || null)}
        className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
      >
        <option value="">All Types</option>
        <option value="image/*">Images</option>
        <option value="application/pdf">PDF</option>
        <option value="application/zip">ZIP</option>
        <option value="text/*">Text Files</option>
      </select>
    </div>
  );
}
