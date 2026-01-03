import type { Example } from '../api/types';
import './ExampleList.css';

interface ExampleListProps {
  examples: Example[];
  loading: boolean;
}

export function ExampleList({ examples, loading }: ExampleListProps) {
  if (loading) {
    return (
      <div className="example-list-loading">
        <div className="loading-spinner" />
        <span>Loading examples...</span>
      </div>
    );
  }

  if (examples.length === 0) {
    return (
      <div className="example-list-empty">
        <span className="empty-icon">📭</span>
        <p>No examples found</p>
      </div>
    );
  }

  return (
    <ul className="example-list">
      {examples.map((example, index) => (
        <li
          key={example.id}
          className="example-item animate-slide-up"
          style={{ animationDelay: `${index * 50}ms` }}
        >
          <div className="example-header">
            <span className="example-name">{example.name}</span>
            <span className={`example-status ${example.active ? 'active' : 'inactive'}`}>
              {example.active ? 'Active' : 'Inactive'}
            </span>
          </div>
          {example.description && <p className="example-description">{example.description}</p>}
          <div className="example-meta">
            <span className="meta-item">
              <span className="meta-label">ID:</span>
              <code>{example.id}</code>
            </span>
            <span className="meta-item">
              <span className="meta-label">Created:</span>
              <span>{formatDate(example.createdAt)}</span>
            </span>
          </div>
        </li>
      ))}
    </ul>
  );
}

function formatDate(dateString: string): string {
  try {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  } catch {
    return dateString;
  }
}
