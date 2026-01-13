import { Link } from 'react-router-dom';
import { Logo } from './Logo';

interface LogoBrandProps {
  to?: string;
  size?: 'sm' | 'md' | 'lg';
}

const sizes = {
  sm: { logo: 24, text: 'text-lg' },
  md: { logo: 28, text: 'text-xl' },
  lg: { logo: 36, text: 'text-2xl' },
};

export function LogoBrand({ to = '/', size = 'md' }: LogoBrandProps) {
  const { logo, text } = sizes[size];

  const content = (
    <>
      <Logo size={logo} />
      <span
        className={`${text} font-semibold bg-gradient-to-r from-indigo-600 to-purple-600 bg-clip-text text-transparent`}
      >
        Starter
      </span>
    </>
  );

  return (
    <Link to={to} className="flex items-center gap-2">
      {content}
    </Link>
  );
}
