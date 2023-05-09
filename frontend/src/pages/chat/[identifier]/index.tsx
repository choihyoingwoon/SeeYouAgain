import React, { useRef, useState, useEffect, ChangeEvent, FormEvent } from 'react';
import { useRouter } from 'next/router';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import { axAuth } from '@/apis/axiosinstance';
import Image from 'next/image';
import ChatBox from '@/components/ChatBox';
import { AiOutlinePlusCircle } from 'react-icons/ai';
import { IoMdSend } from 'react-icons/io';
import Button from '@/components/Button';

interface ChatData {
    messageId: number;
    writerId: number;
    profileImg: string;
    chat: string;
    isRead: boolean;
    createdAt: string;
    updatedAt: string;
}

interface ChannelInfo {
    productId: number;
    productImg: string;
    title: string;
    price: number;
    nickname: string;
    mannerScore: number;
    productState: boolean;
    type: boolean;
}

function Channel() {
    const router = useRouter();
    const [chatList, setChatList] = useState<ChatData[]>([]);
    const [chat, setChat] = useState<string>('');
    const [firstMessageId, setFirstMessageId] = useState<number>();
    const [channelInfo, setChannelInfo] = useState<ChannelInfo>();

    const { identifier } = router.query;
    const client = useRef<Client | null>(null);

    const connect = () => {
        const socket = new SockJS('http://k8c101.p.ssafy.io:8000/chatting-service/ws');
        // const socket = new SockJS('http://localhost:8000/chatting-service/ws');

        client.current = new Client({
            webSocketFactory: () => socket,
            debug: str => console.log(str),
        });

        client.current.onConnect = () => {
            console.log('success');
            subscribe();
        };

        client.current.activate();
    };

    const subscribe = () => {
        client.current?.subscribe('/sub/chat/' + identifier, body => {
            const json_body: ChatData = JSON.parse(body.body);
            setChatList((_chat_list: ChatData[]) => [json_body, ..._chat_list]);
        });
    };

    const publish = (chat: string) => {
        if (!client.current?.connected) return;

        client.current.publish({
            destination: '/pub/chat',
            body: JSON.stringify({
                identifier: identifier,
                writerId: 2,
                chat: chat,
            }),
        });

        setChat('');
    };

    const disconnect = () => {
        let lastReadMessageId = 0;

        if (chatList.length > 0) {
            lastReadMessageId = chatList[0].messageId;
        }

        axAuth({
            url: `/chatting-service/auth/participant/out/${identifier}/${lastReadMessageId}`,
            method: 'patch',
        });
        // .then(res => {});

        client.current?.deactivate();
    };

    const handleChange = (event: ChangeEvent<HTMLInputElement>) => {
        setChat(event.target.value);
    };

    const handleSubmit = (event: FormEvent<HTMLFormElement>, chat: string) => {
        event.preventDefault();

        if (chat.trim()) {
            publish(chat);
        } else {
            setChat('');
        }
    };

    const getMessage = () => {
        let api;
        if (firstMessageId) {
            api = `/chatting-service/auth/channel/chat/${identifier}/${firstMessageId}`;
        } else {
            api = `/chatting-service/auth/channel/chat/${identifier}`;
        }

        axAuth({
            url: api,
        }).then(res => {
            if (res.data.data.length > 0) {
                const messageList = res.data.data;
                setChatList((_chat_list: ChatData[]) => [..._chat_list, ...messageList]);
                setFirstMessageId(messageList[messageList.length - 1].messageId);
            }
        });
    };

    const getChannelInfo = () => {
        axAuth({
            url: `/chatting-service/auth/channel/detail/${identifier}`,
        }).then(res => {
            setChannelInfo(res.data.data);
        });
    };

    const saveReadMessageSize = () => {
        axAuth({
            url: `/chatting-service/auth/participant/in/${identifier}`,
            method: 'patch',
        });
        // .then(res => {});
    };

    useEffect(() => {
        if (!router.isReady) return;

        saveReadMessageSize();
        getChannelInfo();
        getMessage();
        connect();

        return () => disconnect();
    }, [router.isReady]);

    return (
        <div className="relative pt-48">
            <div className="fixed inset-x-0 top-0 bg-white z-50">
                {channelInfo && (
                    <div className="p-5 text-center border-b border-gray flex justify-center items-center">
                        <p className="me-1 font-bold">{channelInfo?.nickname}</p>
                        <Button.MannerPoint innerValue={`${channelInfo?.mannerScore}`} />
                    </div>
                )}
                <div className="pt-4 px-4 pb-2 border-b border-gray">
                    <div className="flex items-center">
                        <div className="m-2 relative" style={{ width: 50, height: 50 }}>
                            {channelInfo?.productImg && <Image src={channelInfo.productImg} alt="물품 이미지" className="rounded-md object-cover" fill />}
                        </div>
                        <div>
                            <p>{channelInfo?.title}</p>
                            <p className="text-sm">
                                <span className="font-bold">{channelInfo?.price}원</span>
                                <span className="text-gray-400"> /일</span>
                            </p>
                        </div>
                    </div>
                    <div>
                        <button className="border border-blue px-3 my-1 text-blue rounded-lg">예약하기</button>
                    </div>
                </div>
            </div>

            <div className="chat-list mx-5 pb-16">
                {chatList
                    .slice()
                    .reverse()
                    .map(
                        (chatData, index): React.ReactNode => (
                            <ChatBox key={index} chat={chatData.chat} profileImg={chatData.profileImg} writerId={chatData.writerId} userId={2} isRead={chatData.isRead} />
                        ),
                    )}
            </div>

            <div className="fixed inset-x-0 bottom-0 bg-white">
                <form onSubmit={event => handleSubmit(event, chat)}>
                    <div className="flex items-center p-2 relative">
                        <div className="me-2">
                            <AiOutlinePlusCircle className="text-3xl" />
                        </div>
                        <input type={'text'} name={'chatInput'} onChange={handleChange} value={chat} placeholder="메세지를 입력하세요." className="w-11/12 bg-gray-200 py-2 ps-3 pe-10 rounded-full" />
                        <button className="absolute right-3 flex justify-center items-center rounded-full p-1.5" style={{ background: '#5669ff' }}>
                            <IoMdSend className="text-white text-lg -rotate-90" />
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}

export default Channel;
