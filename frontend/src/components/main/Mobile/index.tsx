import React from 'react';
import FifthPage from '../Mobile/FifthPage';
import SeventhPage from '../Mobile/SeventhPage';
import SecondPage from '../Mobile/SecondPage';
import ThirdPage from '../Mobile/ThirdPage';
import FourthPage from '../Mobile/FourthPage';
import SixthPage from '../Mobile/SixthPage';
import FirstPage from '../Mobile/FirstPage';
import styles from '../Mobile/main.module.scss';
import classNames from 'classnames';
import Image from 'next/image';
import logo from '@/images/logo.png';

export default function index() {
    return (
        <div>
            <div className={classNames('fixed top-0 h-16 w-full z-30')}>
                <Image src={logo} alt="logo" width={300} className="m-auto mt-4" />
            </div>
            <div className={styles.main_body}>
                <div className={classNames(styles.box, styles.point)}>
                    <FirstPage />
                </div>
                <div className={classNames(styles.box, styles.point)}>
                    <SecondPage />
                </div>
                <div className={classNames(styles.box, styles.none)}>
                    <ThirdPage />
                </div>
                <div className={classNames(styles.box, styles.none)}>
                    <FourthPage />
                </div>
                <div className={classNames(styles.box_nohidden, styles.none, 'pt-16')}>
                    <FifthPage />
                </div>
                <div className={classNames(styles.box, styles.blue)}>
                    <SixthPage />
                </div>
                <div className={classNames(styles.box, styles.none)}>
                    <SeventhPage />
                </div>
            </div>
            <div className={classNames('fixed bottom-0 h-20 w-full p-3 mb-3', styles.header)}>
                <button className={classNames(styles.btn, 'rounded-xl bg-white')}>START</button>
            </div>
        </div>
    );
}