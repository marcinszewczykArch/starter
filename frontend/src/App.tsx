import { useState, useEffect } from 'react';
import { ExampleList } from './components/ExampleList';
import { ApiStatus } from './components/ApiStatus';
import { MetricsDashboard } from './components/MetricsDashboard';
import { exampleApi } from './api/exampleApi';
import type { Example } from './api/types';
import './App.css';

function App() {
  const [examples, setExamples] = useState<Example[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchExamples = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await exampleApi.getAll();
      setExamples(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch examples');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchExamples();
  }, []);

  return (
    <div className="app">
      <header className="app-header">
        <div className="container">
          <div className="header-content">
            <div className="logo-section">
              <div className="logo">
                <span className="logo-icon">⚡</span>
                <span className="logo-text">Starter</span>
              </div>
              <span className="version-badge">v0.0.1</span>
            </div>
            <nav className="nav">
              <a href="/swagger-ui/index.html" target="_blank" rel="noopener noreferrer">
                API Docs
              </a>
              <a href="/actuator/health" target="_blank" rel="noopener noreferrer">
                Health
              </a>
            </nav>
          </div>
        </div>
      </header>

      <main className="app-main">
        <div className="container">
          <section className="hero animate-slide-up">
            <h1 className="hero-title">
              Welcome to <span className="highlight">Starter</span>
            </h1>
            <p className="hero-description">
              A production-ready monorepo template with Spring Boot backend and React frontend.
            </p>
          </section>

          <div className="content-grid">
            <section
              className="card status-card animate-slide-up"
              style={{ animationDelay: '100ms' }}
            >
              <h2 className="card-title">API Status</h2>
              <ApiStatus />
            </section>

            <section
              className="card examples-card animate-slide-up"
              style={{ animationDelay: '200ms' }}
            >
              <div className="card-header">
                <h2 className="card-title">Examples from Database</h2>
                <button className="refresh-btn" onClick={fetchExamples} disabled={loading}>
                  {loading ? '↻' : '⟳'} Refresh
                </button>
              </div>

              {error && <div className="error-message">{error}</div>}

              <ExampleList examples={examples} loading={loading} />
            </section>
          </div>

          <section className="tech-stack animate-slide-up" style={{ animationDelay: '300ms' }}>
            <h2 className="section-title">Tech Stack</h2>
            <div className="tech-grid">
              <div className="tech-item">
                <span className="tech-icon">☕</span>
                <span className="tech-name">Java 21</span>
              </div>
              <div className="tech-item">
                <span className="tech-icon">🍃</span>
                <span className="tech-name">Spring Boot 3</span>
              </div>
              <div className="tech-item">
                <span className="tech-icon">🐘</span>
                <span className="tech-name">PostgreSQL</span>
              </div>
              <div className="tech-item">
                <span className="tech-icon">⚛️</span>
                <span className="tech-name">React 18</span>
              </div>
              <div className="tech-item">
                <span className="tech-icon">📦</span>
                <span className="tech-name">Vite</span>
              </div>
              <div className="tech-item">
                <span className="tech-icon">🔷</span>
                <span className="tech-name">TypeScript</span>
              </div>
            </div>
          </section>

          <section className="animate-slide-up" style={{ animationDelay: '400ms' }}>
            <MetricsDashboard />
          </section>
        </div>
      </main>

      <footer className="app-footer">
        <div className="container">
          <p>Built with ♥ using modern web technologies</p>
        </div>
      </footer>
    </div>
  );
}

export default App;
