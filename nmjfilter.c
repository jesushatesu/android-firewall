/*
 * 	
 *		этот файл добавится в net/core и будет использоваться для отправки нужных данных о входящих или исходящих skb
 *		
 */

//TODO:убрать лишние хидеры

#include <asm/uaccess.h>
#include <linux/bitops.h>
#include <linux/capability.h>
#include <linux/cpu.h>
#include <linux/types.h>
#include <linux/kernel.h>
#include <linux/hash.h>
#include <linux/slab.h>
#include <linux/sched.h>
#include <linux/mutex.h>
#include <linux/string.h>
#include <linux/mm.h>
#include <linux/socket.h>
#include <linux/sockios.h>
#include <linux/errno.h>
#include <linux/interrupt.h>
#include <linux/if_ether.h>
#include <linux/netdevice.h>
#include <linux/etherdevice.h>
#include <linux/ethtool.h>
#include <linux/notifier.h>
#include <linux/skbuff.h>
#include <net/net_namespace.h>
#include <net/sock.h>
#include <linux/rtnetlink.h>
#include <linux/netlink.h>
#include <linux/stat.h>
#include <net/dst.h>
#include <net/dst_metadata.h>
#include <net/pkt_sched.h>
#include <net/checksum.h>
#include <net/xfrm.h>
#include <linux/highmem.h>
#include <linux/init.h>
#include <linux/module.h>
#include <linux/netpoll.h>
#include <linux/rcupdate.h>
#include <linux/delay.h>
#include <net/iw_handler.h>
#include <asm/current.h>
#include <linux/audit.h>
#include <linux/dmaengine.h>
#include <linux/err.h>
#include <linux/ctype.h>
#include <linux/if_arp.h>
#include <linux/if_vlan.h>
#include <linux/ip.h>
#include <net/ip.h>
#include <net/mpls.h>
#include <linux/ipv6.h>
#include <linux/in.h>
#include <linux/jhash.h>
#include <linux/random.h>
#include <trace/events/napi.h>
#include <trace/events/net.h>
#include <trace/events/skb.h>
#include <linux/pci.h>
#include <linux/inetdevice.h>
#include <linux/cpu_rmap.h>
#include <linux/static_key.h>
#include <linux/hashtable.h>
#include <linux/vmalloc.h>
#include <linux/if_macvlan.h>
#include <linux/errqueue.h>
#include <linux/hrtimer.h>
#include <linux/netfilter_ingress.h>
#include <linux/tcp.h>
#include <net/tcp.h>

//#include "nmjfilter.h" потом добавить

struct sock *nl_sk = NULL;
int inputPid;
extern struct net init_net;



void print_skb_info(struct sk_buff *skb)
{
	struct iphdr *ip;
	struct nmj_buff *nmj;
	nmj = kmalloc(sizeof(struct nmj_buff), GFP_USER);
	
	strcpy(nmj->name, skb->dev->name);
	
	/*
	printk(KERN_INFO "nmjfilter -____- name: %s",
				skb->dev->name);
	
				
	switch (skb->protocol) {
	case htons(ETH_P_IPV6):
		printk("nmjfilter skb MAC info IPv6 over bluebook");
		break;
	case htons(ETH_P_MAP):
		printk("nmjfilter skb MAC info Qualcomm multiplexing and aggregation protocol");
		break;
	case htons(ETH_P_IP):
		printk("nmjfilter skb MAC info Internet Protocol packet");
	}*/
	
	ip = (struct iphdr *)skb_network_header(skb);
	
	nmj->protocol = ip->protocol;
	nmj->ip_version = ip->version;
	
	if (ip->version == 4)
	{
		nmj->saddr = ntohl(ip->saddr);
		nmj->daddr = ntohl(ip->daddr);
	
		if (ip->protocol == IPPROTO_TCP)
		{
			struct tcphdr *tcp;
			
			/* Задаем смещение в байтах для указателя на TCP заголовок */
			/* ip->ihl - длина IP заголовка в 32-битных словах */
			skb_set_transport_header(skb, ip->ihl * 4);
			
			/* Сохраняем указатель на структуру заголовка TCP */
			tcp = (struct tcphdr *)skb_transport_header(skb);
			
			nmj->source = tcp->source;
			nmj->dest = tcp->dest;
			/*
			printk(KERN_INFO "nmjfilter TCP - protocol: %d, saddr: %d.%d.%d.%d:%d, daddr: %d.%d.%d.%d:%d ",ip->protocol, saddr>>24, (saddr>>16)&0x00FF,(saddr>>8)&0x0000FF, (saddr)&0x000000FF, sport, daddr>>24, (daddr>>16)&0x00FF,(daddr>>8)&0x0000FF, (daddr)&0x000000FF, dport);*/
			
	    }
	    if (ip->protocol == IPPROTO_UDP)
		{
			struct udphdr *udp;
			
			skb_set_transport_header(skb, ip->ihl * 4);
			udp = (struct udphdr *)skb_transport_header(skb);
			
			nmj->source = udp->source;
			nmj->dest = udp->dest;
			/*
			printk(KERN_INFO "nmjfilter UDP - protocol: %d, saddr: %d.%d.%d.%d:%d, daddr: %d.%d.%d.%d:%d ",ip->protocol, saddr>>24, (saddr>>16)&0x00FF,(saddr>>8)&0x0000FF, (saddr)&0x000000FF, sport, daddr>>24, (daddr>>16)&0x00FF,(daddr>>8)&0x0000FF, (daddr)&0x000000FF, dport);*/
		}
		if (ip->protocol == IPPROTO_ICMP)
		{
		/*
			printk(KERN_INFO "nmjfilter ICMP - protocol: %d, saddr: %d.%d.%d.%d, daddr: %d.%d.%d.%d ",ip->protocol, saddr>>24, (saddr>>16)&0x00FF,(saddr>>8)&0x0000FF, (saddr)&0x000000FF, daddr>>24, (daddr>>16)&0x00FF,(daddr>>8)&0x0000FF, (daddr)&0x000000FF);*/
		}
		
	}
	printk(KERN_INFO "nmj_buff - name: %s, protocol: %d, ip_version: %d, saddr: %d.%d.%d.%d:%d, daddr: %d.%d.%d.%d:%d ", nmj->name, nmj->protocol, nmj->ip_version, nmj->saddr>>24, (nmj->saddr>>16)&0x00FF,(nmj->saddr>>8)&0x0000FF, (nmj->saddr)&0x000000FF, nmj->source, nmj->daddr>>24, (nmj->daddr>>16)&0x00FF,(nmj->daddr>>8)&0x0000FF, (nmj->daddr)&0x000000FF, nmj->dest);
}

static void nl_recv_msg(struct sk_buff *skb)
{

    struct nlmsghdr *nlh;
    char *inputData = NULL;

    nlh = (struct nlmsghdr *)skb->data;
    inputData = (char *)nlmsg_data(nlh);
    
    inputPid = nlh->nlmsg_pid; /*pid of sending process */

    //some things
}



static void nl_send_msg(char *msg, uint16_t len)
{
    struct sk_buff *skb_out;
    struct nlmsghdr *nlh;
    int res;
	
    skb_out = nlmsg_new(len, GFP_ATOMIC);
    if (!skb_out) {
        printk(KERN_ERR "Failed to allocate new skb\n");
        return -1;
    }

    nlh = nlmsg_put(skb_out, 0, 0, NLMSG_DONE, len, 0);
    if(nlh == NULL)
    {
        printk("nlmsg_put failaure \n");
        nlmsg_free(skb_out);
        return -1;
    }
    
    memcpy(nlmsg_data(nlh), pbuf, len);
    ret = netlink_unicast(nl_sk, skb_out, inputPid, MSG_DONTWAIT);
    if (res < 0)
        printk(KERN_INFO "Error while sending back to user\n");
}

static int __init nmjfilter_init(void)
{

    printk("Entering: %s\n", __FUNCTION__);
    
    struct netlink_kernel_cfg cfg = {
        .input = nl_recv_msg,
    };

    nl_sk = netlink_kernel_create(&init_net, NETLINK_USER, &cfg);
    if (!nl_sk) {
        printk(KERN_ALERT "Error creating socket.\n");
        return -10;
    }

    return 0;
}



subsys_initcall(nmjfilter_init);
